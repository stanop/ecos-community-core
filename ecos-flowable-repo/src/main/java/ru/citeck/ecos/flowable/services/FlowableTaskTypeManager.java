package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.task.Task;

/**
 * Flowable task type manager interface
 */
public interface FlowableTaskTypeManager {

    /**
     * Get start task definition
     * @param taskTypeName Task type name
     * @return Type definition
     */
    TypeDefinition getStartTaskDefinition(String taskTypeName);

    /**
     * Get full task definition
     * @param task Task
     * @return Type definition
     */
    TypeDefinition getFullTaskDefinition(Task task);

    /**
     * Get full task definition
     * @param delegateTask Delegate task
     * @return Type definition
     */
    TypeDefinition getFullTaskDefinition(DelegateTask delegateTask);

    /**
     * Get full task definition
     * @param typeName Task type name
     * @return Type definition
     */
    TypeDefinition getFullTaskDefinition(String typeName);

}
