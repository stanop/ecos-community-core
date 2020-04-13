package ru.citeck.ecos.icase.activity.service.eproc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventDelegate;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorDto;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorService;
import ru.citeck.ecos.records2.evaluator.evaluators.GroupEvaluator;
import ru.citeck.ecos.utils.EvaluatorUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EProcCaseActivityEventDelegate implements CaseActivityEventDelegate {

    private EProcActivityService eprocActivityService;
    private EProcCaseActivityListenerManager listenerManager;
    private EProcCaseEvaluatorConverter eprocCaseEvaluatorConverter;
    private RecordEvaluatorService recordEvaluatorService;

    @Autowired
    public EProcCaseActivityEventDelegate(EProcActivityService eprocActivityService,
                                          EProcCaseActivityListenerManager listenerManager,
                                          EProcCaseEvaluatorConverter eprocCaseEvaluatorConverter,
                                          RecordEvaluatorService recordEvaluatorService) {
        this.eprocActivityService = eprocActivityService;
        this.listenerManager = listenerManager;
        this.eprocCaseEvaluatorConverter = eprocCaseEvaluatorConverter;
        this.recordEvaluatorService = recordEvaluatorService;
    }

    @Override
    public void fireEvent(ActivityRef activityRef, String eventType) {
        List<SentryDefinition> sentryDefs = eprocActivityService.findSentriesBySourceRefAndEventType(
                activityRef.getProcessId(), activityRef.getId(), eventType);

        for (SentryDefinition sentryDef : sentryDefs) {
            fireConcreteEventImpl(activityRef.getProcessId(), sentryDef);
        }
    }

    @Override
    public void fireConcreteEvent(EventRef eventRef) {
        SentryDefinition sentryDefinition = eprocActivityService.getSentryDefinition(eventRef);
        fireConcreteEventImpl(eventRef.getProcessId(), sentryDefinition);
    }

    private void fireConcreteEventImpl(RecordRef caseRef, SentryDefinition sentryDefinition) {
        if (checkConditionsImpl(caseRef, sentryDefinition)) {
            EventRef eventRef = EventRef.of(CaseServiceType.EPROC, caseRef, sentryDefinition.getId());
            listenerManager.beforeEventFired(eventRef);
            listenerManager.onEventFired(eventRef);
        }
    }

    @Override
    public boolean checkConditions(EventRef eventRef) {
        SentryDefinition sentryDefinition = eprocActivityService.getSentryDefinition(eventRef);
        return checkConditionsImpl(eventRef.getProcessId(), sentryDefinition);
    }

    private boolean checkConditionsImpl(RecordRef caseRef, SentryDefinition sentryDefinition) {
        RecordEvaluatorDto evaluatorDefinition = convertEvaluatorDefinition(sentryDefinition.getEvaluator());
        if (evaluatorDefinition != null) {
            return recordEvaluatorService.evaluate(caseRef, evaluatorDefinition);
        }
        return true;
    }

    private RecordEvaluatorDto convertEvaluatorDefinition(EvaluatorDefinition definition) {
        EvaluatorDefinitionData[] definitionDataArray = definition.getData().getAs(EvaluatorDefinitionData[].class);
        if (definitionDataArray == null || definitionDataArray.length == 0) {
            return null;
        } else if (definitionDataArray.length == 1) {
            return eprocCaseEvaluatorConverter.convertCondition(definitionDataArray[0]);
        } else {
            GroupEvaluator.Config config = new GroupEvaluator.Config();
            config.setJoinBy(GroupEvaluator.JoinType.AND);
            List<RecordEvaluatorDto> groupedEvaluators = Arrays.stream(definitionDataArray)
                    .map(eprocCaseEvaluatorConverter::convertCondition)
                    .collect(Collectors.toList());
            config.setEvaluators(groupedEvaluators);
            return EvaluatorUtils.createEvaluatorDto("group", config, false);
        }
    }

}
