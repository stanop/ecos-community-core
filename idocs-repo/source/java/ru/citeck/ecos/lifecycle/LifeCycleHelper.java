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
package ru.citeck.ecos.lifecycle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuter;
import org.alfresco.repo.workflow.StartWorkflowActionExecuter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.citeck.ecos.action.FailActionExecuter;
import ru.citeck.ecos.action.SetLifecycleProcessVariableActionExecuter;
import ru.citeck.ecos.action.evaluator.CompareLifecycleProcessVariableValueEvaluator;
import ru.citeck.ecos.action.evaluator.UserHasPermissionEvaluator;
import ru.citeck.ecos.action.evaluator.UserInDocumentEvaluator;
import ru.citeck.ecos.action.evaluator.UserInGroupEvaluator;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleEvent;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleState;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleTransition;

public class LifeCycleHelper {

    private static Log logger = LogFactory.getLog(LifeCycleHelper.class);

	public static List<LifeCycleState> getStatesByStateId(LifeCycleDefinition lcd, String stateId) {
		List<LifeCycleState> result = new ArrayList<LifeCycleState>();

		if (stateId != null) {
			for (LifeCycleState lcState : lcd.getStateList()) {
				if ("*".equals(lcState.getId()) || stateId.equals(lcState.getId()))
					result.add(lcState);
			}
		}

		return result;
	}

	public static List<LifeCycleTransition> getTransitionsByFromStateId(LifeCycleDefinition lcd, String fromState) {
		List<LifeCycleTransition> result = new ArrayList<LifeCycleTransition>();

		if (fromState != null) {
			for (LifeCycleTransition transition : lcd.getTransitionList()) {
				if ("*".equals(transition.getFromState()) || fromState.equals(transition.getFromState()))
					result.add(transition);
			}
		}

		return result;
	}

	public static List<LifeCycleState> filterStatesByEventType(List<LifeCycleState> states, Set<String> eventTypes) {
		List<LifeCycleState> filteredStates = new ArrayList<LifeCycleState>();

		for (LifeCycleState state : states) {
			LifeCycleEvent lcEvent = state.getEvent();

			if ((lcEvent != null) && (lcEvent.getEventType() != null)) {
				for (String eventType : eventTypes) {
					if (lcEvent.getEventType().equals(eventType))
						filteredStates.add(state);
				}
			}
		}

		return filteredStates;
	}

	public static List<LifeCycleTransition> filterTransitionsByEventType(List<LifeCycleTransition> transitions, Set<String> eventTypes) {
		List<LifeCycleTransition> filteredTransitions = new ArrayList<LifeCycleTransition>();

		for (LifeCycleTransition transition : transitions) {
			LifeCycleEvent lcEvent = transition.getEvent();

			if ((lcEvent != null) && (lcEvent.getEventType() != null)) {
				for (String eventType : eventTypes) {
					if (lcEvent.getEventType().equals(eventType))
						filteredTransitions.add(transition);
				}
			}
		}

		return filteredTransitions;
	}

	public static List<String> getJSONStringAsList(String jsonString) {
	    try {
            JSONArray jsonArr = new JSONArray(jsonString);

            List<String> resultList = new ArrayList<String>();

            for (int i = 0; i < jsonArr.length(); i++)
                resultList.add(jsonArr.getString(i));

            return resultList;
        } catch (JSONException e) {
            logger.error("Can't parse JSON", e);
        }

        return null;
	}

	@SuppressWarnings("rawtypes")
    public static Map<String, Object> getJSONStringAsMap(String jsonString) {
	    try {
            JSONObject jsonObj = new JSONObject(jsonString);
            Iterator jsonKeys = jsonObj.keys();

            Map<String, Object> resultMap = new HashMap<String, Object>();

            while (jsonKeys.hasNext()) {
                String key = (String) jsonKeys.next();
                resultMap.put(key, jsonObj.get(key));
            }

            return resultMap;
        } catch (JSONException e) {
            logger.error("Can't parse JSON", e);
        }

	    return null;
	}

	public static String getConditionEvaluatorName(String conditionType) {
	    String actionConditionName = null;

        switch (conditionType) {
            case LifeCycleConstants.VAL_USER_IN_DOCUMENT:
                actionConditionName = UserInDocumentEvaluator.NAME;
                break;
            case LifeCycleConstants.VAL_USER_IN_GROUP:
                actionConditionName = UserInGroupEvaluator.NAME;
                break;
            case LifeCycleConstants.VAL_USER_HAS_PERMISSION:
                actionConditionName = UserHasPermissionEvaluator.NAME;
                break;
            case LifeCycleConstants.VAL_PROCESS_VARIABLE:
                actionConditionName = CompareLifecycleProcessVariableValueEvaluator.NAME;
                break;
            case LifeCycleConstants.VAL_DOCUMENT_ATTRIBUTE:
                actionConditionName = ComparePropertyValueEvaluator.NAME;
                break;
        }

        return actionConditionName;
	}

	public static String getActionExecutorName(String actionType) {
	    String executorName = actionType;

	    switch (actionType) {
    	    case LifeCycleConstants.VAL_DOCUMENT_ATTRIBUTE:
                executorName = SetPropertyValueActionExecuter.NAME;
                break;
    	    case LifeCycleConstants.VAL_START_PROCESS:
                executorName = StartWorkflowActionExecuter.NAME;
                break;
    	    case LifeCycleConstants.VAL_SEND_EMAIL:
	            executorName = MailActionExecuter.NAME;
	            break;
    	    case LifeCycleConstants.VAL_PROCESS_VARIABLE:
                executorName = SetLifecycleProcessVariableActionExecuter.NAME;
                break;
    	    case LifeCycleConstants.VAL_FAIL:
                executorName = FailActionExecuter.NAME;
                break;
	    }

	    return executorName;
	}

	/**
     * Converts parameter value to type required by condition evaluator implementation
     */
	public static Serializable getPreparedConditionParameter(String actionType, String paramName, String paramValue, ServiceRegistry serviceRegistry) {
        if (LifeCycleConstants.VAL_DOCUMENT_ATTRIBUTE.equals(actionType)) {
            if (ComparePropertyValueEvaluator.PARAM_PROPERTY.equals(paramName))
                return QName.createQName(paramValue, serviceRegistry.getNamespaceService());
            else
                return paramValue;
        }

        return paramValue;
    }

	/**
	 * Converts parameter value to type required by native executer implementations
	 */
	public static Serializable getPreparedActionParameter(String actionType, String paramName, String paramValue, ServiceRegistry serviceRegistry) {
	    if (LifeCycleConstants.VAL_DOCUMENT_ATTRIBUTE.equals(actionType)) {
	        if (SetPropertyValueActionExecuter.PARAM_PROPERTY.equals(actionType))
	            return QName.createQName(paramValue, serviceRegistry.getNamespaceService());
	    } else if (LifeCycleConstants.VAL_START_PROCESS.equals(actionType)) {
            if (StartWorkflowActionExecuter.PARAM_END_START_TASK.equals(actionType))
                return Boolean.valueOf(paramValue);
	    } else if (LifeCycleConstants.VAL_SEND_EMAIL.equals(actionType)) {
	        if (MailActionExecuter.PARAM_TO_MANY.equals(paramName))
	            return (Serializable) getJSONStringAsList(paramValue);
	        else if (MailActionExecuter.PARAM_IGNORE_SEND_FAILURE.equals(paramName))
                return Boolean.valueOf(paramValue);
	        else if (MailActionExecuter.PARAM_SEND_AFTER_COMMIT.equals(paramName))
                return Boolean.valueOf(paramValue);
	        else if (MailActionExecuter.PARAM_TEMPLATE.equals(paramName))
	            return new NodeRef(paramValue);
	        else if (MailActionExecuter.PARAM_TEMPLATE_MODEL.equals(paramName))
                return (Serializable) getJSONStringAsMap(paramValue);
	    }

	    return paramValue;
	}
}
