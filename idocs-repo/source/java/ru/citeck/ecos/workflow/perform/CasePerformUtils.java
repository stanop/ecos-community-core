package ru.citeck.ecos.workflow.perform;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.CasePerformModel;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Simonov
 */
public class CasePerformUtils {

    public static final String DEFAULT_DELIMITER = ",";

    public static final String OPTIONAL_PERFORMERS = "optionalPerformers";
    public static final String EXCLUDED_PERFORMERS = "excludedPerformers";
    public static final String TASKS_PERFORMERS = "tasksPerformers";
    public static final String MANDATORY_TASKS = "mandatoryTasks";
    public static final String ABORT_PERFORMING = "abortPerforming";
    public static final String SKIP_PERFORMING = "skipPerforming";
    public static final String PERFORMERS = "performers";

    private static final List<String> VARIABLES_SHARING_IGNORED_PREFIXES = Arrays.asList("bpm", "cwf", "wfcf", "cm");
    private static final Pattern VARIABLES_PATTERN = Pattern.compile("^([^_]+)_(.+)");

    private NodeService nodeService;
    private NamespaceService namespaceService;
    private AuthorityService authorityService;
    private DictionaryService dictionaryService;
    private Repository repositoryHelper;

    boolean isCommentMandatory(ExecutionEntity execution, TaskEntity task) {
        return isInSplitString(execution, CasePerformModel.PROP_OUTCOMES_WITH_MANDATORY_COMMENT,
                                  task, CasePerformModel.PROP_PERFORM_OUTCOME);
    }

    boolean isAbortOutcomeReceived(ExecutionEntity execution, TaskEntity task) {
        return isInSplitString(execution, CasePerformModel.PROP_ABORT_OUTCOMES,
                                  task, CasePerformModel.PROP_PERFORM_OUTCOME);
    }

    void saveTaskPerformers(ExecutionEntity execution, TaskEntity task) {

        Map<String, Set<NodeRef>> tasksPerformers = getMap(execution, TASKS_PERFORMERS);
        Set<NodeRef> performers = tasksPerformers.get(task.getId());
        if (performers == null) {
            performers = new HashSet<>();
            tasksPerformers.put(task.getId(), performers);
        }
        performers.addAll(getTaskPerformers(task));

    }

    void saveTaskResult(ExecutionEntity execution, TaskEntity task) {

        String outcome = (String)task.getVariableLocal(toString(CasePerformModel.PROP_PERFORM_OUTCOME));
        NodeRef person = repositoryHelper.getPerson();
        String userName = (String)nodeService.getProperty(person, ContentModel.PROP_USER_USERNAME);
        String resultName = "perform-result-" + userName;

        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(CasePerformModel.PROP_RESULT_OUTCOME, outcome);
        properties.put(CasePerformModel.PROP_RESULT_DATE, new Date());
        properties.put(ContentModel.PROP_NAME, resultName);

        NodeRef bpmPackage = ((ScriptNode) execution.getVariable("bpm_package")).getNodeRef();

        QName assocQName = QName.createQName(CasePerformModel.NAMESPACE, resultName);
        NodeRef result = nodeService.createNode(bpmPackage, CasePerformModel.ASSOC_PERFORM_RESULTS,
                                                assocQName, CasePerformModel.TYPE_PERFORM_RESULT,
                                                properties).getChildRef();

        nodeService.createAssociation(result, person, CasePerformModel.ASSOC_RESULT_PERSON);

        Map<String, Set<NodeRef>> performersByTask = getMap(execution, TASKS_PERFORMERS);
        Set<NodeRef> performers = performersByTask.get(task.getId());

        for (NodeRef performer : performers) {
            nodeService.createAssociation(result, performer, CasePerformModel.ASSOC_RESULT_PERFORMER);
        }
    }

    Set<NodeRef> getTaskPerformers(TaskEntity task) {

        Set<IdentityLink> candidates = task.getCandidates();
        String assigneeName = task.getAssignee();
        Set<NodeRef> performers = new HashSet<>();

        if (assigneeName != null) {
            performers.add(authorityService.getAuthorityNodeRef(assigneeName));
        }

        for (IdentityLink candidate : candidates) {
            NodeRef nodeRef = toNodeRef(candidate);
            if (nodeRef != null) {
                performers.add(nodeRef);
            }
        }

        return performers;
    }

    void shareVariables(VariableScope from, VariableScope to) {
        Map<String, Object> variables = from.getVariables();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (isSharedVariable(entry.getKey())) {
                to.setVariable(entry.getKey(), entry.getValue());
            }
        }
    }

    boolean isSharedVariable(String name) {
        Matcher matcher = VARIABLES_PATTERN.matcher(name);
        return matcher.matches() && !VARIABLES_SHARING_IGNORED_PREFIXES.contains(matcher.group(1));
    }

    boolean isInSplitString(VariableScope stringScope, QName stringKey,
                                   VariableScope valueScope, QName valueKey) {

        String[] values = getSplitString(stringScope, stringKey);
        String value = (String) valueScope.getVariableLocal(toString(valueKey));

        for (String stringValue : values) {
            if (stringValue.equals(value)) {
                return true;
            }
        }

        return false;
    }

    <T> Collection<T> getCollection(VariableScope scope, String key) {
        return getVariable(scope, key, Collection.class, ArrayList.class);
    }

    <K,V> Map<K,V> getMap(VariableScope scope, String key) {
        return getVariable(scope, key, Map.class, HashMap.class);
    }

    <T,D> T getVariable(VariableScope scope, String key, Class<T> clazz, Class<D> defaultClass) {
        Object var;
        if (scope.hasVariable(key)) {
            var = scope.getVariable(key);
            if (clazz.isInstance(var)) {
                return (T)var;
            }
        }
        try {
            T defaultVar = (T)defaultClass.newInstance();
            scope.setVariable(key, defaultVar);
            return defaultVar;
        } catch (Exception e) {
            return null;
        }
    }

    String[] getSplitString(VariableScope scope, QName key) {
        return getSplitString(scope, toString(key));
    }

    String[] getSplitString(VariableScope scope, String key) {
        String variable = (String)scope.getVariable(key);
        if (variable == null) {
            return new String[0];
        }
        return variable.split(DEFAULT_DELIMITER);
    }

    String toString(QName qname) {
        return qname.toPrefixString(namespaceService).replaceAll(":", "_");
    }

    NodeRef toNodeRef(IdentityLink identityLink) {
        String id = identityLink.getGroupId();
        if (id == null) {
            id = identityLink.getTaskId();
        }
        return id != null ? authorityService.getAuthorityNodeRef(id) : null;
    }

    Set<NodeRef> getContainedAuthorities(NodeRef container, AuthorityType type, boolean recurse) {

        QName containerType = nodeService.getType(container);
        if (dictionaryService.isSubClass(containerType, ContentModel.TYPE_AUTHORITY_CONTAINER)) {
            String groupName = (String) nodeService.getProperty(container, ContentModel.PROP_AUTHORITY_NAME);
            Set<String> authorities = authorityService.getContainedAuthorities(type, groupName, !recurse);
            Set<NodeRef> authoritiesRefs = new HashSet<>();

            for (String authority : authorities) {
                authoritiesRefs.add(authorityService.getAuthorityNodeRef(authority));
            }

            return authoritiesRefs;
        }
        return Collections.emptySet();
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }
}
