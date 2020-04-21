package ru.citeck.ecos.icase.timer;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.model.CaseTimerModel;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.AlfActivityUtils;

import java.util.Date;

/**
 * @author Pavel Simonov
 */
@Service
public class CaseTimerService {

    private NodeService nodeService;
    private CaseActivityService caseActivityService;
    private CaseActivityEventService caseActivityEventService;
    private AlfActivityUtils alfActivityUtils;
    private CaseTimerEvaluatorService evaluatorService;


    @Autowired
    public CaseTimerService(ServiceRegistry serviceRegistry,
                            CaseTimerEvaluatorService evaluatorService) {
        this.nodeService = serviceRegistry.getNodeService();
        this.caseActivityService = (CaseActivityService) serviceRegistry
                .getService(CiteckServices.CASE_ACTIVITY_SERVICE);
        this.caseActivityEventService = (CaseActivityEventService) serviceRegistry
                .getService(CiteckServices.CASE_ACTIVITY_EVENT_SERVICE);
        this.alfActivityUtils = (AlfActivityUtils) serviceRegistry.getService(CiteckServices.ALF_ACTIVITY_UTILS);
        this.evaluatorService = evaluatorService;
    }

    public boolean startTimer(NodeRef timerRef) {
        return startTimer(timerRef, evaluatorService.getFromDate(timerRef), 0);
    }

    private boolean startTimer(NodeRef timerRef, Date fromDate, int repeatCounter) {
        String computed = evaluatorService.evaluateExpression(timerRef, fromDate, repeatCounter);
        Date occurDate = evaluatorService.getNextOccurDate(computed, fromDate, repeatCounter);

        if (occurDate != null) {
            nodeService.setProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE, occurDate);
            nodeService.setProperty(timerRef, CaseTimerModel.PROP_REPEAT_COUNTER, repeatCounter);
            nodeService.setProperty(timerRef, CaseTimerModel.PROP_COMPUTED_EXPRESSION, computed);
            return true;
        }
        return false;
    }

    public void stopTimer(NodeRef timerRef) {
        nodeService.setProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE, null);
        nodeService.setProperty(timerRef, CaseTimerModel.PROP_REPEAT_COUNTER, 0);
        nodeService.setProperty(timerRef, CaseTimerModel.PROP_COMPUTED_EXPRESSION, null);
    }

    public void timerOccur(NodeRef timerRef) {
        ActivityRef timerActivityRef = alfActivityUtils.composeActivityRef(timerRef);
        CaseActivity activity = caseActivityService.getActivity(timerActivityRef);

        if (activity.isActive()) {

            Date fromDate = (Date) nodeService.getProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE);
            int counter = getRepeatCounter(timerRef) + 1;

            if (!startTimer(timerRef, fromDate, counter)) {
                caseActivityService.stopActivity(timerActivityRef);
            } else {
                caseActivityEventService.fireEvent(timerActivityRef, ICaseEventModel.CONSTR_ACTIVITY_STOPPED);
            }
        }
    }

    public int getRepeatCounter(NodeRef timerRef) {
        Integer counter = (Integer) nodeService.getProperty(timerRef, CaseTimerModel.PROP_REPEAT_COUNTER);
        return counter != null ? Math.max(0, counter) : 0;
    }

    public boolean isTimerValid(NodeRef timerRef) {
        Date fromDate = evaluatorService.getFromDate(timerRef);
        String expressionResult = evaluatorService.evaluateExpression(timerRef, fromDate, 0);
        Date occurDate = evaluatorService.getNextOccurDate(expressionResult, fromDate, 0);
        return occurDate != null;
    }

    public boolean isActive(NodeRef timerRef) {
        Date occurDate = (Date) nodeService.getProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE);
        return occurDate != null;
    }

    public boolean isOccurred(NodeRef timerRef) {
        Date occurDate = (Date) nodeService.getProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE);
        return occurDate != null && occurDate.getTime() <= System.currentTimeMillis();
    }
}
