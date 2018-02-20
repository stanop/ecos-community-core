package ru.citeck.ecos.flowable.listeners.global.impl.task.create;

import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.flowable.engine.FormService;
import org.flowable.identitylink.service.IdentityLinkType;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.engine.form.FormData;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import ru.citeck.ecos.flowable.converters.FlowablePropertyConverter;
import ru.citeck.ecos.flowable.listeners.global.GlobalCreateTaskListener;

/**
 * Task create listener
 */
public class TaskCreateListener implements GlobalCreateTaskListener {

    /**
     * Constants
     */
    private static final String NODE_WORKSPACE_PREFIX = "workspace://SpacesStore/";

    /**
     * Flowable property converter
     */
    private FlowablePropertyConverter propertyConverter;

    /**
     * Form service
     */
    private FormService formService;

    /**
     * Person service
     */
    private PersonService personService;

    /**
     * Notify
     * @param delegateTask Delegate task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        /** Set default properties */
        propertyConverter.setDefaultTaskProperties(delegateTask);

        String taskFormKey = getFormKey(delegateTask);
        TypeDefinition typeDefinition = propertyConverter.getFactory().getTaskTypeDefinition(taskFormKey, false);
        taskFormKey = typeDefinition.getName().toPrefixString();
        delegateTask.setVariableLocal(ActivitiConstants.PROP_TASK_FORM_KEY, taskFormKey);

        /** Check assignee */
        if (delegateTask.getAssignee() != null) {
            if (delegateTask.getAssignee().startsWith(NODE_WORKSPACE_PREFIX)) {
                NodeRef initiatorNode = new NodeRef(delegateTask.getAssignee());
                PersonService.PersonInfo initiatorInfo = personService.getPerson(initiatorNode);
                if (initiatorInfo != null && initiatorInfo.getUserName() != null) {
                    delegateTask.setAssignee(initiatorInfo.getUserName());
                }
            }
        }

        /** Set initiator variable */
        NodeRef initiatorNode = (NodeRef) delegateTask.getVariable(WorkflowConstants.PROP_INITIATOR);
        if(initiatorNode != null) {
            delegateTask.addUserIdentityLink(personService.getPerson(initiatorNode).getUserName(),
                    IdentityLinkType.STARTER);
        }
    }

    /**
     * Get form key from task
     * @param task Delegate task
     * @return Form key
     */
    private String getFormKey(DelegateTask task) {
        FormData formData = null;
        TaskEntity taskEntity = (TaskEntity) task;
        if (taskEntity != null) {
            formData = formService.getTaskFormData(task.getId());
            if (formData != null) {
                return formData.getFormKey();
            }
        }
        return null;
    }

    /**
     * Set property converter
     * @param propertyConverter Property converter
     */
    public void setPropertyConverter(FlowablePropertyConverter propertyConverter) {
        this.propertyConverter = propertyConverter;
    }

    /**
     * Set form service
     * @param formService Form service
     */
    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    /**
     * Set person service
     * @param personService Person service
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
