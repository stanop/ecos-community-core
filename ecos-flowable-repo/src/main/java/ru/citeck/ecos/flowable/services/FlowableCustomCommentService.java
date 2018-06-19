package ru.citeck.ecos.flowable.services;

import java.util.List;

/**
 * Flowable custom comment service interface
 */
public interface FlowableCustomCommentService {

    /**
     * Get comments fields by task id
     * @param taskId Task id
     * @return List of comment fields
     */
    List<String> getFieldIdsByTaskId(String taskId);

    /**
     * Get comments fields by process definitions id
     * @param processDefinitionId Process definition id
     * @return List of comment fields
     */
    List<String> getFieldIdsByProcessDefinitionId(String processDefinitionId);
}
