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
package ru.citeck.ecos.action;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

import ru.citeck.ecos.lifecycle.LifeCycleServiceImpl;

/**
 * Set Lifecycle Process Variable Action Executer
 *
 * @author Alexey Moiseyev
 */
public class SetLifecycleProcessVariableActionExecuter extends ActionExecuterAbstractBase {

    /**
     * Action executor constants
     */
    public static final String NAME = "set-lifecycle-process-variable-action-executer";
    public static final String PARAM_VARIABLE = "variable";
    public static final String PARAM_VALUE = "value";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        String variableName = (String) action.getParameterValue(PARAM_VARIABLE);
        String variableValue = (String) action.getParameterValue(PARAM_VALUE);

        if (variableName != null) {
            if (AlfrescoTransactionSupport.isActualTransactionActive()) {
                Map<String, Object> processVariables = AlfrescoTransactionSupport.getResource(LifeCycleServiceImpl.PROCESS_VARS);

                if (processVariables != null)
                    processVariables.put(variableName, variableValue);
                else
                    Logger.getLogger(getClass()).error("Process variables are undefined. Make sure you call this action executor in lifecycle context.");
            } else
                Logger.getLogger(getClass()).error("Actual transaction is not active. Unable to modify lifecycle process variable " + variableName);
        } else
            Logger.getLogger(getClass()).error("Variable name is empty. Unable to modify lifecycle process variables");
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_VARIABLE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_VARIABLE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_VALUE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_VALUE)));
    }
}