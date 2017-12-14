package ru.citeck.ecos.flowable.listeners.global.impl.process.start;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.listeners.global.GlobalStartExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;

/**
 * Set initiator start process listener
 */
public class SetInitiatorStartProcessListener implements GlobalStartExecutionListener {

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Person service
     */
    private PersonService personService;

    /**
     * Notify
     * @param delegateExecution Execution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {
        Object setInitiator = delegateExecution.getVariable("cwf_setInitiator");
        if (Boolean.TRUE.equals(setInitiator)) {
            NodeRef docRef = FlowableListenerUtils.getDocument(delegateExecution, nodeService);
            if (docRef == null) {
                return;
            }
            String docCreatorUserName = (String) nodeService.getProperty(docRef, ContentModel.PROP_CREATOR);
            NodeRef creatorUser = personService.getPerson(docCreatorUserName);
            if(creatorUser!=null) {
                delegateExecution.setVariable("initiator", creatorUser);
            }
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
}
