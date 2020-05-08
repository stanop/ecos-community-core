package ru.citeck.ecos.icase.timer.evaluator;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.icase.activity.dto.ActivityInstance;
import ru.citeck.ecos.icase.timer.CaseTimerEvaluatorService;
import ru.citeck.ecos.model.CaseTimerModel.ExpressionType;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfActivityUtils;

import java.util.Date;

/**
 * @author Pavel Simonov
 */
public abstract class Evaluator {

    protected NodeService nodeService;
    protected AlfActivityUtils alfActivityUtils;
    private CaseTimerEvaluatorService evaluatorService;

    public void init() {
        evaluatorService.registerEvaluator(getType(), this);
    }

    public abstract ExpressionType getType();

    public abstract String evaluate(NodeRef timerRef, Date fromDate, int repeatCounter);

    public abstract String evaluate(RecordRef caseRef, ActivityInstance activityInstance,
                                    Date fromDate, int repeatCounter);

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
    }

    @Autowired
    public void setEvaluatorService(CaseTimerEvaluatorService evaluatorService) {
        this.evaluatorService = evaluatorService;
    }
}
