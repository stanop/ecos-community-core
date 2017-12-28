package ru.citeck.ecos.flowable.services.impl;

import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.flowable.engine.FormService;
import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.form.FormData;
import org.flowable.engine.form.TaskFormData;
import org.flowable.engine.impl.persistence.entity.TaskEntity;
import org.flowable.engine.task.Task;
import ru.citeck.ecos.flowable.services.FlowableTaskTypeManager;

/**
 * Flowable task type manager
 */
public class FlowableTaskTypeManagerImpl implements FlowableTaskTypeManager  {

    /**
     * Workflow object factory
     */
    private final WorkflowObjectFactory factory;

    /**
     * Form service
     */
    private final FormService formService;

    /**
     * Constructor
     * @param factory Workflow object factory
     * @param formService Form service
     */
    public FlowableTaskTypeManagerImpl(WorkflowObjectFactory factory, FormService formService) {
        this.factory = factory;
        this.formService = formService;
    }

    /**
     * Get start task definition
     * @param taskTypeName Task type name
     * @return Type definition
     */
    @Override
    public TypeDefinition getStartTaskDefinition(String taskTypeName) {
        return factory.getTaskFullTypeDefinition(taskTypeName, true);
    }

    /**
     * Get full task definition
     * @param task Task
     * @return Type definition
     */
    @Override
    public TypeDefinition getFullTaskDefinition(Task task) {
        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        return getFullTaskDefinition(task.getId(), taskFormData);
    }

    /**
     * Get full task definition
     * @param delegateTask Delegate task
     * @return Type definition
     */
    public TypeDefinition getFullTaskDefinition(DelegateTask delegateTask) {
        FormData formData = null;
        TaskEntity taskEntity = (TaskEntity) delegateTask;

        if (taskEntity != null) {
            formData = formService.getTaskFormData(taskEntity.getId());
        }
        return getFullTaskDefinition(delegateTask.getId(), formData);
    }

    /**
     * Get full task definition
     * @param typeName Task type name
     * @return Type definition
     */
    public TypeDefinition getFullTaskDefinition(String typeName) {
        return getFullTaskDefinition(typeName, null);
    }

    /**
     * Get full task definition
     * @param taskDefinitionKey Task definition key
     * @param taskFormData Task form data
     * @return Type definition
     */
    private TypeDefinition getFullTaskDefinition(String taskDefinitionKey, FormData taskFormData) {
        String formKey = null;
        if (taskFormData != null) {
            formKey = taskFormData.getFormKey();
        }
        else {
            formKey = taskDefinitionKey;
        }
        return factory.getTaskFullTypeDefinition(formKey, false);
    }
}
