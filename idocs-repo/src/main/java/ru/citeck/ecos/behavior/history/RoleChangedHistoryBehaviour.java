package ru.citeck.ecos.behavior.history;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.model.HistoryModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.role.CaseRolePolicies;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoleChangedHistoryBehaviour implements CaseRolePolicies.OnRoleAssigneesChangedPolicy {

    private static final String CREATED_ROLES_KEY = "createdRoles";
    private static final String REMOVED_ROLES_KEY = "removedRoles";

    private static final String ROLE_EVENT_HISTORY_TYPE = "role.changed";

    private HistoryService historyService;
    private NodeService nodeService;
    private PolicyComponent policyComponent;

    private QName documentQName;
    private String documentNamespace;
    private String documentType;

    private Map<String, Map<String, String>> roleMapping;


    public void init() {
        this.documentQName = QName.createQName(documentNamespace, documentType);
        this.policyComponent.bindClassBehaviour(
                CaseRolePolicies.OnRoleAssigneesChangedPolicy.QNAME,
                ICaseRoleModel.TYPE_ROLE,
                new JavaBehaviour(this, "onRoleAssigneesChanged", Behaviour.NotificationFrequency.EVERY_EVENT)
        );
    }


    @Override
    public void onRoleAssigneesChanged(NodeRef roleRef, Set<NodeRef> added, Set<NodeRef> removed) {
        if (!nodeService.exists(roleRef)) {
            return;
        }

        NodeRef documentRef = nodeService.getPrimaryParent(roleRef).getParentRef();
        QName parentDocumentQName = nodeService.getType(documentRef);
        if (!documentQName.equals(parentDocumentQName)) {
            return;
        }

        String roleVarName = (String) nodeService.getProperty(roleRef, ICaseRoleModel.PROP_VARNAME);

        Map<NodeRef, String> dataForSendingCreatingRoles = getPersonRefsForConcreteAction(CREATED_ROLES_KEY, roleVarName, added);
        for (Map.Entry<NodeRef, String> entry : dataForSendingCreatingRoles.entrySet()) {
            addNoteToHistory(documentRef, entry.getKey(), entry.getValue());
        }

        Map<NodeRef, String> dataForSendingRemovingRoles = getPersonRefsForConcreteAction(REMOVED_ROLES_KEY, roleVarName, removed);
        for (Map.Entry<NodeRef, String> entry : dataForSendingRemovingRoles.entrySet()) {
            addNoteToHistory(documentRef, entry.getKey(), entry.getValue());
        }
    }

    private void addNoteToHistory(NodeRef documentRef, NodeRef personRef, String message) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(HistoryModel.PROP_NAME, ROLE_EVENT_HISTORY_TYPE);
        properties.put(HistoryModel.ASSOC_DOCUMENT, documentRef);
        properties.put(HistoryModel.PROP_TASK_COMMENT, buildComment(personRef, message));

        historyService.persistEvent(HistoryModel.TYPE_BASIC_EVENT, properties);
    }

    private String buildComment(NodeRef personRef, String message) {
        QName confirmerType = nodeService.getType(personRef);
        String confirmer;
        if (confirmerType.equals(ContentModel.TYPE_PERSON)) {
            String firstName = (String) nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
            String lastName = (String) nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
            confirmer = String.format("%s %s", firstName, lastName);
        } else {
            confirmer = (String) nodeService.getProperty(personRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
        }

        return String.format(message, confirmer);
    }

    private Map<NodeRef, String> getPersonRefsForConcreteAction(String action, String roleVarName, Set<NodeRef> persons) {
        if (persons == null || persons.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> mappedRoles = roleMapping.get(action);
        if (mappedRoles == null || mappedRoles.isEmpty()) {
            return Collections.emptyMap();
        }

        String internationalizationMessageKey = mappedRoles.get(roleVarName);
        if (internationalizationMessageKey == null) {
            return Collections.emptyMap();
        }

        Map<NodeRef, String> personRefsWithMessagesMap = new HashMap<>();
        for (NodeRef personRef : persons) {
            personRefsWithMessagesMap.put(personRef, internationalizationMessageKey);
        }
        return personRefsWithMessagesMap;
    }


    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setDocumentNamespace(String documentNamespace) {
        this.documentNamespace = documentNamespace;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setRoleMapping(Map<String, Map<String, String>> roleMapping) {
        this.roleMapping = roleMapping;
    }
}
