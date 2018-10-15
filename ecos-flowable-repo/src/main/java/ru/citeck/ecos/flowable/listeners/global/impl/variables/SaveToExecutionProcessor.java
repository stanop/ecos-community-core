package ru.citeck.ecos.flowable.listeners.global.impl.variables;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This interface provides methods to {@link AbstractFlowableSaveToExecutionListener}
 *
 * @author Roman Makarskiy
 */

public interface SaveToExecutionProcessor {
    boolean saveIsRequired(NodeRef document);
    void saveToExecution(String executionId, NodeRef document);
    void setVariable(String executionId, String variableName, Object value);
}
