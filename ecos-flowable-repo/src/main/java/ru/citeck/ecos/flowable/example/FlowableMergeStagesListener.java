package ru.citeck.ecos.flowable.example;

import org.flowable.engine.common.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import ru.citeck.ecos.utils.JSONUtils;

import java.util.*;

/**
 * Merge stages execution listener
 */
public class FlowableMergeStagesListener implements ExecutionListener {

    private static final String NODE_REF = "nodeRef";
    private static final String CONFIRMERS = "confirmers";
    private static final String STAGES = "stages";

    private Expression stages1;
    private Expression stages2;
    private Expression resultVar;

    @Override
    public void notify(DelegateExecution execution) {

        Map precedence = new HashMap();
        List stages = new LinkedList();
        precedence.put(STAGES, stages);

        Object precedence1 = (Map) JSONUtils.convertJSON(this.stages1.getValue(execution));
        Object precedence2 = (Map) JSONUtils.convertJSON(this.stages2.getValue(execution));

        mergeStages(precedence, (Map) precedence1);
        mergeStages(precedence, (Map) precedence2);

        execution.setVariable(resultVar.getExpressionText(), precedence);

    }

    private void mergeStages(Map targetPrecedence, Map sourcePrecedence) {
        List targetStages = (List) targetPrecedence.get(STAGES);
        List sourceStages = (List) sourcePrecedence.get(STAGES);
        for (int i = 0; i < sourceStages.size(); i++) {
            Map sourceStage = (Map) sourceStages.get(i);
            List sourceConfirmers = (List) sourceStage.get(CONFIRMERS);

            Map targetStage;
            List targetConfirmers;
            if(i < targetStages.size()) {
                targetStage = (Map) targetStages.get(i);
                targetConfirmers = (List) targetStage.get(CONFIRMERS);
            } else {
                targetStage = new HashMap();
                targetStages.add(i, targetStage);
                targetConfirmers = new ArrayList(sourceConfirmers.size());
                targetStage.put(CONFIRMERS, targetConfirmers);
            }

            Set<String> stageConfirmersIndex = new HashSet<String>();
            for (Object confirmerObj : targetConfirmers) {
                Map confirmer = (Map) confirmerObj;
                stageConfirmersIndex.add((String) confirmer.get(NODE_REF));
            }

            for (Object confirmerObj : sourceConfirmers) {
                Map confirmer = (Map) confirmerObj;
                if (!stageConfirmersIndex.contains(confirmer.get(NODE_REF))) {
                    targetConfirmers.add(confirmer);
                }
            }
        }
    }
}
