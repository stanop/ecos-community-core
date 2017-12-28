package ru.citeck.ecos.flowable.services.impl;

import org.apache.commons.collections.CollectionUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.services.FlowableProcessInstanceService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Flowable process instance service
 */
public class FlowableProcessInstanceServiceImpl implements FlowableProcessInstanceService {

    /**
     * Runtime service
     */
    protected RuntimeService runtimeService;

    /**
     * Set runtime service
     * @param runtimeService Runtime service
     */
    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * Check - is start task active
     * @param processInstanceId Process instance id
     * @return Is start task active
     */
    @Override
    public boolean isStartTaskActive(String processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        if (processInstance == null) {
            return false;
        }
        return runtimeService.getVariable(processInstanceId, FlowableConstants.PROP_START_TASK_END_DATE) == null;
    }

    /**
     * Get process instance by id
     * @param processInstanceId Process instance id
     * @return Process instance id
     */
    @Override
    public ProcessInstance getProcessInstanceById(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    /**
     * Get process instances by ids
     * @param processInstanceIds Process instance ids
     * @return List of process instances
     */
    @Override
    public List<ProcessInstance> getProcessInstancesByIds(List<String> processInstanceIds) {
        if (CollectionUtils.isNotEmpty(processInstanceIds)) {
            return runtimeService.createProcessInstanceQuery().processDefinitionIds(new HashSet<>(processInstanceIds)).list();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get all active process instances
     * @return List of process instances
     */
    @Override
    public List<ProcessInstance> getAllActiveProcessInstances() {
        return runtimeService.createProcessInstanceQuery().active().list();
    }
}
