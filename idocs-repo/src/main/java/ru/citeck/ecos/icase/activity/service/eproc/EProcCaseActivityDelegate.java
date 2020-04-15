package ru.citeck.ecos.icase.activity.service.eproc;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.icase.activity.service.CaseActivityDelegate;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnDefinitionConstants;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorDto;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EProcCaseActivityDelegate implements CaseActivityDelegate {

    private EProcActivityService eprocActivityService;
    private EProcCaseActivityListenerManager listenerManager;
    private EProcCaseEvaluatorConverter eprocCaseEvaluatorConverter;
    private RecordEvaluatorService recordEvaluatorService;

    @Autowired
    public EProcCaseActivityDelegate(EProcActivityService eprocActivityService,
                                     EProcCaseActivityListenerManager listenerManager) {
        this.eprocActivityService = eprocActivityService;
        this.listenerManager = listenerManager;
    }

    @Override
    public void startActivity(ActivityRef activityRef) {
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);

        boolean isResetPerformed = false;
        if (needResetBeforeStart(instance)) {
            resetRecursive(activityRef.getProcessId(), instance);
            isResetPerformed = true;
        }

        ActivityTransitionDefinition transitionDefinition = getTransitionDefinition(instance, ActivityState.STARTED);
        if (!isResetPerformed && transitionDefinition == null) {
            return;
        }

        checkTransitionCondition(activityRef.getProcessId(), transitionDefinition);

        instance.setState(ActivityState.STARTED);
        instance.setActivated(new Date());

        listenerManager.beforeStartedActivity(activityRef);
        listenerManager.onStartedActivity(activityRef);

        eprocActivityService.saveState(eprocActivityService.getFullState(activityRef.getProcessId()));
    }

    private boolean needResetBeforeStart(ActivityInstance instance) {
        ActivityState fromState = instance.getState();
        if (fromState != ActivityState.NOT_STARTED) {
            return instance.getDefinition().isRepeatable();
        }
        return false;
    }

    @Override
    public void stopActivity(ActivityRef activityRef) {
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);

        ActivityTransitionDefinition transitionDefinition = getTransitionDefinition(instance, ActivityState.COMPLETED);
        if (transitionDefinition == null) {
            return;
        }

        checkTransitionCondition(activityRef.getProcessId(), transitionDefinition);

        instance.setState(ActivityState.COMPLETED);
        instance.setTerminated(new Date());

        listenerManager.beforeStoppedActivity(activityRef);
        listenerManager.onStoppedActivity(activityRef);

        eprocActivityService.saveState(eprocActivityService.getFullState(activityRef.getProcessId()));
    }

    private ActivityTransitionDefinition getTransitionDefinition(ActivityInstance instance, ActivityState toState) {
        List<ActivityTransitionDefinition> transitions = instance.getDefinition().getTransitions();
        ActivityState fromState = instance.getState();
        return getTransitionDefinition(transitions, fromState, toState);
    }

    private ActivityTransitionDefinition getTransitionDefinition(List<ActivityTransitionDefinition> transitions,
                                                                 ActivityState fromState, ActivityState toState) {
        if (CollectionUtils.isEmpty(transitions)) {
            return null;
        }

        return transitions.stream()
                .filter(transition -> transition.getFromState() == fromState)
                .filter(transition -> transition.getToState() == toState)
                .findFirst().orElse(null);
    }

    private void checkTransitionCondition(RecordRef caseRef, ActivityTransitionDefinition transitionDefinition) {
        RecordEvaluatorDto evaluatorDto = eprocCaseEvaluatorConverter
                .convertEvaluatorDefinition(transitionDefinition.getEvaluator());
        if (evaluatorDto == null) {
            return;
        }

        //TODO
    }

    @Override
    public void reset(ActivityRef activityRef) {
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);
        resetRecursive(activityRef.getProcessId(), instance);
    }

    private void resetRecursive(RecordRef processId, ActivityInstance instance) {
        instance.setState(ActivityState.NOT_STARTED);
        instance.setActivated(null);
        instance.setTerminated(null);

        ActivityRef activityRef = ActivityRef.of(CaseServiceType.EPROC, processId, instance.getId());
        listenerManager.onResetActivity(activityRef);

        if (CollectionUtils.isNotEmpty(instance.getActivities())) {
            for (ActivityInstance childInstance : instance.getActivities()) {
                resetRecursive(processId, childInstance);
            }
        }
    }

    @Override
    public CaseActivity getActivity(ActivityRef activityRef) {
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);
        return toCaseActivity(activityRef, instance);
    }

    @Override
    public List<CaseActivity> getActivities(ActivityRef activityRef) {
        return getActivities(activityRef, false);
    }

    @Override
    public List<CaseActivity> getActivities(ActivityRef activityRef, boolean recurse) {
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);
        List<ActivityInstance> activities = getActivitiesImpl(instance, recurse);
        return activities.stream()
                .map(item -> toCaseActivity(activityRef.getProcessId(), item))
                .sorted(Comparator.comparingInt(CaseActivity::getIndex))
                .collect(Collectors.toList());
    }

    private CaseActivity toCaseActivity(RecordRef caseRef, ActivityInstance instance) {
        ActivityRef activityRef = ActivityRef.of(CaseServiceType.EPROC, caseRef, instance.getId());
        return toCaseActivity(activityRef, instance);
    }

    private CaseActivity toCaseActivity(ActivityRef activityRef, ActivityInstance instance) {
        CaseActivity result = new CaseActivity();
        result.setActivityRef(activityRef);
        result.setTitle(EProcUtils.getAnyAttribute(instance, CmmnDefinitionConstants.TITLE));
        result.setState(instance.getState());
        result.setActive(instance.getState() == ActivityState.STARTED);
        result.setRepeatable(instance.getDefinition().isRepeatable());
        result.setIndex(instance.getDefinition().getIndex());
        return result;
    }

    private List<ActivityInstance> getActivitiesImpl(ActivityInstance instance, boolean recurse) {
        List<ActivityInstance> instances = instance.getActivities();
        if (instances == null) {
            instances = new ArrayList<>();
        }

        if (recurse) {
            for (ActivityInstance childInstance : instances) {
                instances.addAll(getActivitiesImpl(childInstance, true));
            }
        }

        return instances;
    }

    @Override
    public List<CaseActivity> getStartedActivities(ActivityRef activityRef) {
        return getActivities(activityRef).stream()
                .filter(CaseActivity::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public CaseActivity getActivityByName(ActivityRef activityRef, String name, boolean recurse) {
        ActivityInstance instance = eprocActivityService.getStateInstance(activityRef);
        return getActivitiesImpl(instance, recurse).stream()
                .filter(childInstance -> {
                    String instanceName = EProcUtils.getAnyAttribute(instance, CmmnDefinitionConstants.NAME);
                    return StringUtils.equals(name, instanceName);
                })
                .map(childInstance -> toCaseActivity(activityRef.getProcessId(), childInstance))
                .findFirst().orElse(null);
    }

    @Override
    public CaseActivity getActivityByTitle(ActivityRef activityRef, String title, boolean recurse) {
        return getActivities(activityRef, recurse).stream()
                .filter(caseActivity -> StringUtils.equals(caseActivity.getTitle(), title))
                .findFirst().orElse(null);
    }

    @Override
    public void setParent(ActivityRef activityRef, ActivityRef parentRef) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void setParentInIndex(ActivityRef activityRef, int newIndex) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public boolean hasActiveChildren(ActivityRef activityRef) {
        return getActivities(activityRef).stream()
                .anyMatch(CaseActivity::isActive);
    }
}
