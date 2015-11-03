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
package ru.citeck.ecos.action.evaluator;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

import ru.citeck.ecos.lifecycle.LifeCycleServiceImpl;

/**
 * Compare lifecycle process variable value evaluator
 *
 * @author Alexey Moiseyev
 */
public class CompareLifecycleProcessVariableValueEvaluator extends ActionConditionEvaluatorAbstractBase {

    /**
     * Evaluator constants
     */
    public static final String NAME = "compare-lifecycle-process-variable-value-evaluator";
    public static final String PARAM_VARIABLE = "variable";
    public static final String PARAM_VALUE = "value";

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef) {
        String variableName = (String) actionCondition.getParameterValue(PARAM_VARIABLE);
        String variableValue = (String) actionCondition.getParameterValue(PARAM_VALUE);

        if (variableName != null) {
            if (AlfrescoTransactionSupport.isActualTransactionActive()) {
                Map<String, Object> processVariables = AlfrescoTransactionSupport.getResource(LifeCycleServiceImpl.PROCESS_VARS);

                if (processVariables != null) {
                    try {
                        String currentValue = (String) processVariables.get(variableName);

                        if ((currentValue == null) && (variableValue == null))
                            return true;
                        else if ((currentValue != null) && (variableValue != null))
                            return currentValue.equals(variableValue);
                    } catch (ClassCastException e) {
                        Logger.getLogger(getClass()).error("Unable to compare process variable. Only variables of type String are allowed for now.");
                    }
                } else
                    Logger.getLogger(getClass()).error("Process variables are undefined. Make sure you call this action condition evaluator in lifecycle context.");
            } else
                Logger.getLogger(getClass()).error("Actual transaction is not active. Unable to compare lifecycle process variable " + variableName);
        } else
            Logger.getLogger(getClass()).error("Variable name is empty. Unable to compare lifecycle process variable");

        return false;
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_VARIABLE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_VARIABLE), false));
        paramList.add(new ParameterDefinitionImpl(PARAM_VALUE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_VALUE), false));
    }

}
