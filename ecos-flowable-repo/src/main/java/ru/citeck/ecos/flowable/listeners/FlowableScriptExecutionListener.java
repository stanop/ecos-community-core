package ru.citeck.ecos.flowable.listeners;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.scripts.FlowableDelegateExecutionScriptBase;

import java.util.Map;

/**
 * Flowable script execution listener
 */
public class FlowableScriptExecutionListener extends FlowableDelegateExecutionScriptBase {

    /**
     * Constants
     */
    private static final String DELETED_FLAG = "deleted";
    private static final String CANCELLED_FLAG = "cancelled";

    /**
     * Notify
     * @param execution Delegate execution
     */
    public void notify(DelegateExecution execution) {
        validateParameters();
        Object result = null;
        try {
            result = runScript(execution, script.getExpressionText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        /** Set result value */
        if(this.resultVariable != null) {
            execution.setVariable(this.resultVariable.getExpressionText(), result);
        }
    }

    /**
     * Get script input map model
     * @param execution Execution
     * @param runAsUser Run as user
     * @return Script model map
     */
    @Override
    protected Map<String, Object> getInputMap(DelegateExecution execution,
                                              String runAsUser) {
        Map<String, Object> scriptModel =  super.getInputMap(execution, runAsUser);
        ExecutionEntity executionEntity = (ExecutionEntity) execution;

        boolean cancelled = false;
        boolean deleted = false;

        if (FlowableConstants.DELETE_REASON_DELETED.equals(executionEntity.getDeleteReason())) {
            deleted = true;
        }
        else {
            if (FlowableConstants.DELETE_REASON_CANCELLED.equals(executionEntity.getDeleteReason())) {
                cancelled = true;
            }
        }
        scriptModel.put(DELETED_FLAG, deleted);
        scriptModel.put(CANCELLED_FLAG, cancelled);

        return scriptModel;
    }
}
