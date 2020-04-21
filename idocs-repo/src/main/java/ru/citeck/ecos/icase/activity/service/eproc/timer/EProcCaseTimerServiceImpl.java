package ru.citeck.ecos.icase.activity.service.eproc.timer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.commands.timer.RemoteTimerService;
import ru.citeck.ecos.icase.activity.dto.ActivityInstance;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.commands.dto.EProcTimerCommandData;
import ru.citeck.ecos.icase.activity.service.eproc.commands.dto.EProcTimerOccurCommandData;
import ru.citeck.ecos.icase.activity.service.eproc.commands.executors.EProcTimerOccurExecutor;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnInstanceConstants;
import ru.citeck.ecos.icase.timer.CaseTimerEvaluatorService;
import ru.citeck.ecos.records2.RecordRef;

import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
public class EProcCaseTimerServiceImpl implements EProcCaseTimerService {

    private CaseActivityService caseActivityService;
    private CaseTimerEvaluatorService evaluatorService;
    private EProcActivityService eprocActivityService;
    private RemoteTimerService remoteTimerService;

    @Autowired
    public EProcCaseTimerServiceImpl(CaseActivityService caseActivityService,
                                     CaseTimerEvaluatorService evaluatorService,
                                     EProcActivityService eprocActivityService,
                                     RemoteTimerService remoteTimerService) {
        this.caseActivityService = caseActivityService;
        this.evaluatorService = evaluatorService;
        this.eprocActivityService = eprocActivityService;
        this.remoteTimerService = remoteTimerService;
    }

    @Override
    public void scheduleTimer(ActivityRef activityRef) {
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);
        Instant triggerTime = composeTriggerTimeInstant(activityRef.getProcessId(), instance);
        EProcTimerCommandData data = composeCommandCallbackData(activityRef);
        String timerId = remoteTimerService.scheduleTimer(triggerTime, EProcTimerOccurExecutor.TYPE, data);
        EProcUtils.setAttribute(instance, CmmnInstanceConstants.TIMER_ID, timerId);
    }

    private Instant composeTriggerTimeInstant(RecordRef caseRef, ActivityInstance instance) {
        Date fromDate = evaluatorService.getFromDate(instance);
        String expression = evaluatorService.evaluateExpression(caseRef, instance, fromDate, 0);
        Date nextOccurDate = evaluatorService.getNextOccurDate(expression, fromDate, 0);
        if (nextOccurDate == null) {
            throw new RuntimeException("Error while triggering timer, nextOccurDate is null. Instance: " + instance);
        }
        return nextOccurDate.toInstant();
    }

    private EProcTimerCommandData composeCommandCallbackData(ActivityRef activityRef) {
        EProcTimerCommandData data = new EProcTimerCommandData();
        data.setActivityRef(activityRef);
        return data;
    }

    @Override
    public void timerOccurred(EProcTimerOccurCommandData data) {
        if (data.getActivityRef() == null || ActivityRef.EMPTY.equals(data.getActivityRef())) {
            log.warn("Timer with invalid data was occured. Data:'" + data + "'");
            return;
        }

        ActivityInstance instance = eprocActivityService.getStateInstance(data.getActivityRef());
        if (expectedTimerForInstance(data, instance)) {
            EProcUtils.setAttribute(instance, CmmnInstanceConstants.TIMER_ID, null);
            caseActivityService.stopActivity(data.getActivityRef());
        } else {
            log.info("Occurred not expected timer for activity with ref=" + data.getActivityRef());
        }
    }

    private boolean expectedTimerForInstance(EProcTimerOccurCommandData data, ActivityInstance instance) {
        String expectedTimerId = data.getTimerId();
        String actualTimerId = EProcUtils.getAnyAttribute(instance, CmmnInstanceConstants.TIMER_ID);
        return StringUtils.equals(expectedTimerId, actualTimerId);
    }

    @Override
    public void cancelTimer(ActivityRef activityRef) {
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);
        String timerId = EProcUtils.getAnyAttribute(instance, CmmnInstanceConstants.TIMER_ID);
        if (StringUtils.isNotBlank(timerId)) {
            remoteTimerService.cancelTimerAfterTransaction(timerId);
        }
    }

}
