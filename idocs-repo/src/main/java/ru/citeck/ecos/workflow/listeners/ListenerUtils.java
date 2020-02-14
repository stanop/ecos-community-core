/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.workflow.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import ru.citeck.ecos.utils.ReflectionUtils;

import java.util.*;

import static ru.citeck.ecos.utils.WorkflowConstants.VAR_TASK_ORIGINAL_OWNER;

public class ListenerUtils {

    public static final String ACTIVITI_PREFIX = ActivitiConstants.ENGINE_ID + "$";
    public static final String VAR_PACKAGE = "bpm_package";
    public static final String VAR_ATTACHMENTS = "cwf_taskAttachments";
    private static final String TASK_FORM_KEY = "formKey";

    // get workflow package
    public static NodeRef getWorkflowPackage(VariableScope execution) {
        return ((ScriptNode) execution.getVariable(VAR_PACKAGE)).getNodeRef();
    }

    public static NodeRef getWorkflowPackage(WorkflowTask task) {
        return (NodeRef) task.getProperties().get(WorkflowModel.TYPE_PACKAGE);
    }

    public static Object tryGetProcessDefinitionId(DelegateExecution execution) {
        return ReflectionUtils.callGetterIfDeclared(execution, "getProcessDefinitionId", null);
    }

    public static WorkflowDefinition tryGetWorkflowDefinition(DelegateExecution execution, WorkflowService workflowService) {
        WorkflowDefinition workflowDefinition = null;
        Object workflowDefinitionId = tryGetProcessDefinitionId(execution);
        if (workflowDefinitionId != null) {
            workflowDefinition = workflowService.getDefinitionById(ACTIVITI_PREFIX + workflowDefinitionId);
        }
        return workflowDefinition;
    }

    /**
     * @deprecated use bean WorkflowDocumentResolverRegistry
     */
    @Deprecated
    public static NodeRef getDocument(VariableScope execution, NodeService nodeService) {
        NodeRef wfPackage = getWorkflowPackage(execution);
        return getDocumentByPackage(wfPackage, nodeService);
    }

    /**
     * @deprecated use bean WorkflowDocumentResolverRegistry
     */
    @Deprecated
    public static NodeRef getDocumentByPackage(NodeRef wfPackage, NodeService nodeService) {
        if (!nodeService.exists(wfPackage)) {
            return null;
        }

        List<ChildAssociationRef> childAssocs;
        childAssocs = nodeService.getChildAssocs(wfPackage, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
        if (!childAssocs.isEmpty()) {
            return childAssocs.get(0).getChildRef();
        }
        childAssocs = nodeService.getChildAssocs(wfPackage, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        if (!childAssocs.isEmpty()) {
            return childAssocs.get(0).getChildRef();
        }
        return null;
    }

    // get workflow initiator
    public static String getInitiator(VariableScope execution) {
        return (String) ((ScriptNode) execution.getVariable(WorkflowConstants.PROP_INITIATOR)).getProperties().get(ContentModel.PROP_USERNAME);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ArrayList<NodeRef> getPooledActors(DelegateTask task, AuthorityService authorityService) {
        Set<IdentityLink> candidates = (Set) ReflectionUtils.callGetterIfDeclared(task, "getCandidates", new HashSet());
        ArrayList<NodeRef> pooledActors = new ArrayList<>(candidates.size());
        for (IdentityLink candidate : candidates) {
            if (!candidate.getType().equals(IdentityLinkType.CANDIDATE)) {
                continue;
            }
            String userId = candidate.getUserId();
            if (userId != null) {
                NodeRef person = authorityService.getAuthorityNodeRef(userId);
                if (person != null) {
                    pooledActors.add(person);
                }
            }

            String groupId = candidate.getGroupId();
            if (groupId != null) {
                NodeRef group = authorityService.getAuthorityNodeRef(groupId);
                if (group != null) {
                    pooledActors.add(group);
                }
            }
        }
        return pooledActors;
    }

    public static List<NodeRef> getActors(DelegateTask task, AuthorityService authorityService) {
        String assigneeName = task.getAssignee();
        if (StringUtils.isNotBlank(assigneeName)) {
            NodeRef assignee = authorityService.getAuthorityNodeRef(assigneeName);
            return Collections.singletonList(assignee);
        }

        List<NodeRef> pooledActors = getPooledActors(task, authorityService);
        if (CollectionUtils.isEmpty(pooledActors)) {
            return Collections.emptyList();
        }

        String originalOwner = (String) task.getVariableLocal(VAR_TASK_ORIGINAL_OWNER);
        if (StringUtils.isBlank(originalOwner)) {
            return pooledActors;
        }

        NodeRef originalOwnerNodeRef = authorityService.getAuthorityNodeRef(originalOwner);

        if (originalOwnerNodeRef != null) {
            if (pooledActors.contains(originalOwnerNodeRef) && pooledActors.indexOf(originalOwnerNodeRef) != -1) {
                pooledActors.remove(originalOwnerNodeRef);
                pooledActors.add(0, originalOwnerNodeRef);
            }
        }

        return pooledActors;
    }

    public static ArrayList<NodeRef> getTaskAttachments(DelegateTask task) {
        Object taskAttachments = task.getVariable(VAR_ATTACHMENTS);
        if (!(taskAttachments instanceof Collection)) {
            return null;
        }
        @SuppressWarnings("rawtypes")
        Collection source = (Collection) taskAttachments;
        ArrayList<NodeRef> target = new ArrayList<>(source.size());
        for (Object item : source) {
            if (item == null) {
                // empty
            } else if (item instanceof NodeRef) {
                target.add((NodeRef) item);
            } else if (item instanceof ScriptNode) {
                target.add(((ScriptNode) item).getNodeRef());
            } else if (item instanceof String) {
                target.add(new NodeRef((String) item));
            } else {
                throw new IllegalArgumentException("Unsupported task attachment class: " + item.getClass());
            }
        }
        return target;
    }

    public static String getTaskFormKey(DelegateTask task) {
        String formKey = task.getFormKey();
        if (StringUtils.isNotBlank(formKey)) {
            return formKey;
        }

        formKey = (String) task.getVariable(ActivitiConstants.PROP_TASK_FORM_KEY);
        if (StringUtils.isNotBlank(formKey)) {
            return formKey;
        }

        formKey = (String) task.getVariable(TASK_FORM_KEY);
        if (StringUtils.isNotBlank(formKey)) {
            return formKey;
        }

        throw new RuntimeException("Failed get taskForm, from task: " + task.getId());
    }
}
