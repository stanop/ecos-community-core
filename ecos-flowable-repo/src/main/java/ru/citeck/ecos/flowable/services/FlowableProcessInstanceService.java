package ru.citeck.ecos.flowable.services;

import org.flowable.engine.runtime.ProcessInstance;

import java.util.List;

/**
 * Flowable process instance service
 */
public interface FlowableProcessInstanceService {

    /**
     * Check - is start task active
     * @param processInstanceId Process instance id
     * @return Is start task active
     */
    boolean isStartTaskActive(String processInstanceId);

    /**
     * Get process instance by id
     * @param processInstanceId Process instance id
     * @return Process instance id
     */
    ProcessInstance getProcessInstanceById(String processInstanceId);

    /**
     * Get process instances by ids
     * @param processInstanceIds Process instance ids
     * @return List of process instances
     */
    List<ProcessInstance> getProcessInstancesByIds(List<String> processInstanceIds);

    /**
     * Get all active process instances
     * @return List of process instances
     */
    List<ProcessInstance> getAllActiveProcessInstances();
}
