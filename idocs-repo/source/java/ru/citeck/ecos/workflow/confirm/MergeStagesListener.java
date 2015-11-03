/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.workflow.confirm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;

import ru.citeck.ecos.utils.JSONUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MergeStagesListener implements ExecutionListener {
    private static final String NODE_REF = "nodeRef";
    private static final String CONFIRMERS = "confirmers";
    private static final String STAGES = "stages";
    
    private Expression stages1, stages2;
    private Expression resultVar;

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        Map precedence = new HashMap();
        List stages = new LinkedList();
        precedence.put(STAGES, stages);

        Object precedence1 = (Map) JSONUtils.convertJSON(this.stages1.getValue(execution));
        Object precedence2 = (Map) JSONUtils.convertJSON(this.stages2.getValue(execution));

        mergeStages(precedence, (Map) precedence1);
        mergeStages(precedence, (Map) precedence2);

        // finally save precedence:
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
