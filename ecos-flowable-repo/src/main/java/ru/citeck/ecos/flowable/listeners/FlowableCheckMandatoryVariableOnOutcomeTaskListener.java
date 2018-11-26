package ru.citeck.ecos.flowable.listeners;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.common.api.delegate.Expression;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Roman Makarskiy
 */
public class FlowableCheckMandatoryVariableOnOutcomeTaskListener implements TaskListener {

    private static final Log logger = LogFactory.getLog(FlowableCheckMandatoryVariableOnOutcomeTaskListener.class);

    private static final String SEPARATOR = ";";

    private Expression variableId;
    private Expression outcomeId;
    private Expression outcomes;
    private Expression message;

    @Override
    public void notify(DelegateTask delegateTask) {
        VariableValidator validator = new VariableValidator(variableId, outcomeId, outcomes, message, delegateTask);
        validator.check();
    }

    public void setVariableId(Expression variableId) {
        this.variableId = variableId;
    }

    public void setOutcomeId(Expression outcomeId) {
        this.outcomeId = outcomeId;
    }

    public void setOutcomes(Expression outcomes) {
        this.outcomes = outcomes;
    }

    public void setMessage(Expression message) {
        this.message = message;
    }

    private class VariableValidator {
        private String variableId;
        private Object variable;
        private String taskOutcome;
        private String message = "Variable not filled";
        List<String> necessaryOutcomes = new ArrayList<>();

        VariableValidator(Expression variableIdExpr, Expression outcomeIdExpr, Expression necessaryOutcomesExpr,
                          Expression messageExpr, DelegateTask delegateTask) {
            final String outcomeIdStr = expressionToString(outcomeIdExpr);
            final String necessaryOutcomesStr = expressionToString(necessaryOutcomesExpr);
            final String messageStr = expressionToString(messageExpr);

            this.taskOutcome = (String) delegateTask.getVariable(outcomeIdStr);
            this.variableId = expressionToString(variableIdExpr);
            this.variable = delegateTask.getVariable(this.variableId);

            if (messageExpr != null) {
                this.message = Optional.ofNullable(I18NUtil.getMessage(messageStr)).orElse(messageStr);
            }

            if (StringUtils.isNotBlank(necessaryOutcomesStr)) {
                this.necessaryOutcomes = Arrays.asList(necessaryOutcomesStr.split(SEPARATOR));
            }
        }

        public void check() {
            if (logger.isDebugEnabled()) {
                logger.debug("Start check variable"
                        + "\n" + this.toString());
            }

            if (!this.isValid()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Variable not valid");
                }
                throw new VariableNotValidException(message);
            }
        }

        private String expressionToString(Expression expression) {
            return expression != null ? expression.getExpressionText() : "";
        }

        private boolean isValid() {
            if (StringUtils.isBlank(variableId)) {
                throw new IllegalArgumentException("You must set a variableId, it can not be empty.");
            }

            if (necessaryOutcomes.isEmpty()) {
                return variableIsFilled(variable);
            }

            if (StringUtils.isBlank(taskOutcome)) {
                throw new IllegalArgumentException("You must set a outcomeId, it can not be empty, when outcomes filled.");
            }

            if (necessaryOutcomes.contains(taskOutcome)) {
                return variableIsFilled(variable);
            }

            return true;
        }

        private boolean variableIsFilled(Object variable) {
            if (variable == null) {
                return false;
            }

            if (variable instanceof String && StringUtils.isBlank((String) variable)) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return "VariableValidator{" +
                    "variableId='" + variableId + '\'' +
                    ", variable=" + variable +
                    ", taskOutcome='" + taskOutcome + '\'' +
                    ", message='" + message + '\'' +
                    ", necessaryOutcomes=" + necessaryOutcomes +
                    '}';
        }

        private class VariableNotValidException extends RuntimeException {
            VariableNotValidException(String message) {
                super(message);
            }
        }
    }
}
