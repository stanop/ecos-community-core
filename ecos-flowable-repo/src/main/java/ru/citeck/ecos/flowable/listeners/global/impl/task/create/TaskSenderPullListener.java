package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.flowable.task.service.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;
import ru.citeck.ecos.model.CiteckWorkflowModel;

import java.io.Serializable;
import java.util.Map;

/**
 * Task sender pull listener
 */
public class TaskSenderPullListener implements GlobalCreateTaskListener {

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Workflow qname converter
     */
    private WorkflowQNameConverter qNameConverter;

    /**
     * Person service
     */
    private PersonService personService;

    /**
     * Namespace prefix resolver
     */
    private NamespacePrefixResolver namespaceService;

    /**
     * Init
     */
    public void init() {
        qNameConverter = new WorkflowQNameConverter(namespaceService);
    }

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        String varName = qNameConverter.mapQNameToName(CiteckWorkflowModel.PROP_SENDER);
        Object value = delegateTask.getVariable(varName);
        if(value == null) {
            NodeRef initiator = (NodeRef) delegateTask.getVariable(WorkflowConstants.PROP_INITIATOR);
            value = nodeService.getProperty(initiator, ContentModel.PROP_USERNAME);
        }
        delegateTask.setVariableLocal(varName, value);
        updateSenderName(delegateTask, (String) value);
    }

    /**
     * Update sender name
     * @param task Task
     * @param person Username
     */
    private void updateSenderName(DelegateTask task, String person) {
        if (!StringUtils.isEmpty(person) && personService.personExists(person)) {
            NodeRef personNode =  personService.getPerson(person);
            Map<QName, Serializable> properties = nodeService.getProperties(personNode);
            String firstName = (String) properties.get(ContentModel.PROP_FIRSTNAME);
            String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
            String name = firstName;
            if (!StringUtils.isEmpty(lastName)) {
                name += StringUtils.isEmpty(firstName)? lastName : " " + lastName;
            }
            task.setVariableLocal(qNameConverter.mapQNameToName(CiteckWorkflowModel.PROP_SENDER_NAME), name);
        }
    }

    /**
     * Set node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set person service
     * @param personService Person service
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Set namespace prefix resolver
     * @param namespaceService Namespace prefix resolver
     */
    public void setNamespaceService(NamespacePrefixResolver namespaceService) {
        this.namespaceService = namespaceService;
    }
}
