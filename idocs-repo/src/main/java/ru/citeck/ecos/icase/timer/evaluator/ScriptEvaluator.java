package ru.citeck.ecos.icase.timer.evaluator;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.util.ISO8601DateFormat;
import org.mozilla.javascript.Undefined;
import ru.citeck.ecos.model.CaseTimerModel;

import java.util.Date;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class ScriptEvaluator extends Evaluator {

    private ScriptService scriptService;

    @Override
    public CaseTimerModel.ExpressionType getType() {
        return CaseTimerModel.ExpressionType.SCRIPT;
    }

    @Override
    public String evaluate(NodeRef timerRef, Date fromDate, int repeatCounter) {
        String expression = getExpression(timerRef);
        Map<String, Object> model = buildContextModel(timerRef, repeatCounter);
        Object result = scriptService.executeScriptString(expression, model);
        if (result == null) {
            return null;
        }
        if (result instanceof Date) {
            return ISO8601DateFormat.format((Date) result);
        } else if (result instanceof String) {
            return (String) result;
        } else if (result instanceof Undefined) {
            throw new IllegalStateException("Timer script return return nothing. Script: " + expression);
        }
        throw new IllegalStateException("Timer script return incorrect result with type "
                                        + result.getClass() + " but expected Date or String");
    }

    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        super.setServiceRegistry(serviceRegistry);
        scriptService = serviceRegistry.getScriptService();
    }
}
