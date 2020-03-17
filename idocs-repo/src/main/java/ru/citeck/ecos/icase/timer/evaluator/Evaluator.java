package ru.citeck.ecos.icase.timer.evaluator;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.icase.timer.CaseTimerService;
import ru.citeck.ecos.model.CaseTimerModel;
import ru.citeck.ecos.model.CaseTimerModel.ExpressionType;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.service.EcosCoreServices;
import ru.citeck.ecos.utils.AlfActivityUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public abstract class Evaluator {

    private static final String MODEL_TIMER = "timer";
    private static final String MODEL_DOCUMENT = "document";
    private static final String MODEL_REPEAT_COUNTER = "repeatCounter";

    protected NodeService nodeService;
    protected CaseTimerService caseTimerService;
    protected AlfActivityUtils alfActivityUtils;

    public void init() {
        caseTimerService.registerEvaluator(getType(), this);
    }

    public abstract ExpressionType getType();

    public abstract String evaluate(NodeRef timerRef, Date fromDate, int repeatCounter);

    protected String getExpression(NodeRef timerRef) {
        return (String) nodeService.getProperty(timerRef, CaseTimerModel.PROP_TIMER_EXPRESSION);
    }

    protected Map<String, Object> buildContextModel(NodeRef timerRef, int repeatCounter) {

        Map<String, Object> model = new HashMap<>();

        Map<String, Object> variables = ActionConditionUtils.getTransactionVariables();
        for (Map.Entry<String, Object> variable : variables.entrySet()) {
            model.put(variable.getKey(), variable.getValue());
        }
        if (!variables.containsKey(MODEL_DOCUMENT)) {
            model.put(MODEL_DOCUMENT, alfActivityUtils.getDocumentId(timerRef).toString());
        }

        model.put(MODEL_TIMER, timerRef);
        model.put(MODEL_REPEAT_COUNTER, repeatCounter);

        return model;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        caseTimerService = (CaseTimerService) serviceRegistry.getService(EcosCoreServices.CASE_TIMER_SERVICE);
        alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
    }
}
