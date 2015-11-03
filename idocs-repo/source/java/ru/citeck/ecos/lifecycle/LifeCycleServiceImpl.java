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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptProcessor;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;

import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleAction;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleCondition;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleState;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleTransition;
import ru.citeck.ecos.model.LifeCycleModel;

/**
 * @author alexander.nemerov
 * date 19.02.14
 */
public class LifeCycleServiceImpl implements LifeCycleService {

    public static final String PROCESS_VARS = "lifecycle-process-variables";
    private static final String DATE_TIME_EXPRESSION = "dateTimeExpression";
    private static final String WORKFLOW_ID = "workflowId";
    private static final String SIGNAL_ID = "signalId";
    private static Log logger = LogFactory.getLog(LifeCycleServiceImpl.class);

    private NodeService nodeService;
    private SearchService searchService;
    private ScriptProcessor processor;
    private PersonService personService;
    private ServiceRegistry serviceRegistry;
    private Repository repositoryHelper;

    ContentService contentService;

    private Map<String, LifeCycleFormat> formats = new TreeMap<String, LifeCycleFormat>();

    private Map<QName, LifeCycleDefinition> definitions = new HashMap<QName, LifeCycleDefinition>();

    private Map<QName, NodeRef> deployedRepoDefinitions = new HashMap<QName, NodeRef>();

    @Override
    public boolean doTransition(NodeRef nodeRef, String eventType) {
        Set<String> filters = new HashSet<String>();
        filters.add(eventType);

        List<LifeCycleTransition> transitions = getTransitionsByEventTypesAndTrueConditions(nodeRef, filters);
        LifeCycleTransition transition = chooseRightTransition(nodeRef, transitions);

        if (transition != null) {
        	String fromState = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_STATE);
            String toState = transition.getToState();

        	List<LifeCycleState> fromStateDefs = getStateDefsByStateIdEventTypes(nodeRef, fromState, filters);
        	List<LifeCycleState> toStateDefs = getStateDefsByStateIdEventTypes(nodeRef, toState, filters);

        	LifeCycleState fromStateDef = chooseAppropriateStateDef(fromStateDefs, null, null, "string", nodeRef);
        	LifeCycleState toStateDef = chooseAppropriateStateDef(toStateDefs, null, null, "string", nodeRef);

        	return doTransition(nodeRef, transition, fromStateDef, toStateDef);
        }

        return false;
    }

    @Override
    public boolean doTransition(final NodeRef nodeRef, final LifeCycleTransition transition,
    		final LifeCycleState fromStateDef, final LifeCycleState toStateDef) {
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
            @Override
            public Boolean doWork() throws Exception {
                String fromState = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_STATE); // due to possible * in transition def
                String toState = transition.getToState();

                if (nodeService.hasAspect(nodeRef, LifeCycleModel.ASPECT_HAS_TIMER)) {
                    nodeService.removeAspect(nodeRef, LifeCycleModel.ASPECT_HAS_TIMER);
                }

                if (fromState != null && toState != null) {
                	// execute end actions for old state
                	if (fromStateDef != null) {
	                	for (LifeCycleAction action : fromStateDef.getEndActionList()) {
	                    	performAction(nodeRef, action);
	                    }
                	}

                	// execute transition actions
                    for (LifeCycleAction action : transition.getActionList()) {
                    	performAction(nodeRef, action);
                    }

                    nodeService.setProperty(nodeRef, LifeCycleModel.PROP_STATE, toState);

                    // execute start actions for new state
                    if (toStateDef != null) {
	                	for (LifeCycleAction action : toStateDef.getStartActionList()) {
	                    	performAction(nodeRef, action);
	                    }
                    }

                    // do automatic transition if exists
                    boolean transited = doTransition(nodeRef, LifeCycleModel.CONSTR_AUTOMATIC_TRANSITION);

                    // do timer transition if no more auto transitions
                    if (!transited) {
                        doTimerTransition(nodeRef);
                    }

                    return true;
                }

                return false;
            }
        });
    }

    private boolean doTransitionOnProcess(final NodeRef nodeRef, final String processType, final Map<String, Object> model,
    		final String processEvent) {

    	AlfrescoTransactionSupport.bindResource(PROCESS_VARS, model);

        try {
            return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
                @Override
                public Boolean doWork() throws Exception {
                    Set<String> filters = new HashSet<String>();
                    filters.add(processEvent);

                    List<LifeCycleTransition> transitionsForType = getTransitionsByEventTypesAndTrueConditions(nodeRef, filters);

                    List<LifeCycleTransition> transitionsForProcess = new ArrayList<LifeCycleTransition>();

                    if (processType != null) {
	                    for (LifeCycleTransition transition : transitionsForType) {
	                    	if (processType.equals(transition.getEvent().getEventParam(WORKFLOW_ID)))
	                            transitionsForProcess.add(transition);
	                    }

	                    LifeCycleTransition transition = chooseRightTransition(nodeRef, transitionsForProcess);

	                    if (transition != null) {
	                    	String fromState = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_STATE); // due to possible * in transition def
	                        String toState = transition.getToState();

	                    	List<LifeCycleState> fromStateDefs = getStateDefsByStateIdEventTypes(nodeRef, fromState, filters);
	                    	List<LifeCycleState> toStateDefs = getStateDefsByStateIdEventTypes(nodeRef, toState, filters);

	                    	LifeCycleState fromStateDef = chooseAppropriateStateDef(fromStateDefs, WORKFLOW_ID, processType, "string", nodeRef);
	                    	LifeCycleState toStateDef = chooseAppropriateStateDef(toStateDefs, WORKFLOW_ID, processType, "string", nodeRef);

	                    	return doTransition(nodeRef, transition, fromStateDef, toStateDef);
	                    }
                    }

                    return false;
                }
            });
        } finally {
            AlfrescoTransactionSupport.unbindResource(PROCESS_VARS);
        }
    }

    @Override
    public boolean doTransitionOnStartProcess(final NodeRef nodeRef, final String processType, final Map<String, Object> model) {
        return doTransitionOnProcess(nodeRef, processType, model, LifeCycleModel.CONSTR_TRANSITION_ON_START_PROCESS);
    }

    @Override
    public boolean doTransitionOnEndProcess(final NodeRef nodeRef, final String processType, final Map<String, Object> model) {
    	return doTransitionOnProcess(nodeRef, processType, model, LifeCycleModel.CONSTR_TRANSITION_ON_END_PROCESS);
    }

    @Override
    public boolean doTransitionOnSignal(final NodeRef nodeRef, final String signalId, final Map<String, Object> model) {
    	AlfrescoTransactionSupport.bindResource(PROCESS_VARS, model);

        try {
            return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
                @Override
                public Boolean doWork() throws Exception {
                    Set<String> filters = new HashSet<String>();
                    filters.add(LifeCycleModel.CONSTR_TRANSITION_ON_SIGNAL);

                    List<LifeCycleTransition> transitionsForType = getTransitionsByEventTypesAndTrueConditions(nodeRef, filters);

                    List<LifeCycleTransition> transitionsForSignal = new ArrayList<LifeCycleTransition>();

                    if (signalId != null) {
	                    for (LifeCycleTransition transition : transitionsForType) {
	                    	if (signalId.equals(transition.getEvent().getEventParam(SIGNAL_ID)))
	                            transitionsForSignal.add(transition);
	                    }

	                    LifeCycleTransition transition = chooseRightTransition(nodeRef, transitionsForSignal);

	                    if (transition != null) {
	                    	String fromState = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_STATE); // due to possible * in transition def
	                        String toState = transition.getToState();

	                    	List<LifeCycleState> fromStateDefs = getStateDefsByStateIdEventTypes(nodeRef, fromState, filters);
	                    	List<LifeCycleState> toStateDefs = getStateDefsByStateIdEventTypes(nodeRef, toState, filters);

	                    	LifeCycleState fromStateDef = chooseAppropriateStateDef(fromStateDefs, SIGNAL_ID, signalId, "string", nodeRef);
	                    	LifeCycleState toStateDef = chooseAppropriateStateDef(toStateDefs, SIGNAL_ID, signalId, "string", nodeRef);

	                    	return doTransition(nodeRef, transition, fromStateDef, toStateDef);
	                    }
                    }

                    return false;
                }
            });
        } finally {
            AlfrescoTransactionSupport.unbindResource(PROCESS_VARS);
        }
    }

    @Override
    public boolean doTimerTransition(final NodeRef nodeRef) {
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
            @Override
            public Boolean doWork() throws Exception {
		        Set<String> filters = new HashSet<String>();
		        filters.add(LifeCycleModel.CONSTR_TIMER_TRANSITION);

		        if (getTransitionsByDocStateEventTypes(nodeRef, filters).size() > 0)
		            nodeService.addAspect(nodeRef, LifeCycleModel.ASPECT_HAS_TIMER, new HashMap<QName, Serializable>());

		        List<LifeCycleTransition> transitionsForType = getTransitionsByEventTypesAndTrueConditions(nodeRef, filters);

		        List<LifeCycleTransition> transitionsWhenTimeElapsed = new ArrayList<LifeCycleTransition>();

		        for (LifeCycleTransition transition : transitionsForType) {
		            try {
		                String expression = transition.getEvent().getEventParam(DATE_TIME_EXPRESSION);
		                Date eventDateTime = (Date) doJS(nodeRef, expression);

		                if (eventDateTime != null) {
			                if (eventDateTime.getTime() <= (Calendar.getInstance().getTime().getTime()))
			                    transitionsWhenTimeElapsed.add(transition);

			                Date docDateTime = (Date) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_EVENT_TIME);

			                if (docDateTime == null || eventDateTime.getTime() < docDateTime.getTime())
			                    nodeService.setProperty(nodeRef, LifeCycleModel.PROP_EVENT_TIME, eventDateTime.getTime());
		                }
		            } catch (ClassCastException e) {
		                logger.error("Can't cast to Date", e);
		            }
		        }

		        LifeCycleTransition transition = chooseRightTransition(nodeRef, transitionsWhenTimeElapsed);
		        boolean result = false;

		        if (transition != null) {
                	String fromState = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_STATE); // due to possible * in transition def
                    String toState = transition.getToState();

                	List<LifeCycleState> fromStateDefs = getStateDefsByStateIdEventTypes(nodeRef, fromState, filters);
                	List<LifeCycleState> toStateDefs = getStateDefsByStateIdEventTypes(nodeRef, toState, filters);

                	Date now = Calendar.getInstance().getTime();
                	LifeCycleState fromStateDef = chooseAppropriateStateDef(fromStateDefs, DATE_TIME_EXPRESSION, now, "dateTimeLE", nodeRef);
                	LifeCycleState toStateDef = chooseAppropriateStateDef(toStateDefs, DATE_TIME_EXPRESSION, now, "dateTimeLE", nodeRef);

                	result = doTransition(nodeRef, transition, fromStateDef, toStateDef);
                }

		        if (result)
		            nodeService.removeAspect(nodeRef, LifeCycleModel.ASPECT_HAS_TIMER);

		        return result;
            }
        });
    }

    private LifeCycleTransition chooseRightTransition(NodeRef nodeRef, List<LifeCycleTransition> transitions) {
        List<LifeCycleTransition> possibleTransitions = new ArrayList<LifeCycleTransition>();

        for (LifeCycleTransition transition : transitions) {
            if (checkConditions(nodeRef, transition))
                possibleTransitions.add(transition);
        }

        if (possibleTransitions.size() == 0) {
            return null;
        } else if (possibleTransitions.size() == 1) {
            return possibleTransitions.get(0);
        } else {
            for (LifeCycleTransition possibleTransition : possibleTransitions) {
            	List<LifeCycleCondition> conditions = possibleTransition.getConditionList();

        		for (LifeCycleCondition condition : conditions) {
        			if (condition.getType() != null && condition.getParamsCount() > 0) {
                        return possibleTransition;
                    }
        		}
            }
        }

        return null;
    }

    private LifeCycleState chooseAppropriateStateDef(List<LifeCycleState> stateList, String eventParamName,
    		Object eventParamValue, String compareType, NodeRef nodeRef) {
    	List<LifeCycleState> filteredStateList = new ArrayList<LifeCycleState>();

    	if ((stateList != null) && (eventParamName != null) && (eventParamValue != null)) {
	    	for (LifeCycleState state : stateList) {
	    		if ("string".equals(compareType)) {
		        	if (((String) eventParamValue).equals(state.getEvent().getEventParam(eventParamName)))
		                filteredStateList.add(state);
	    		} else if ("dateTimeLE".equals(compareType)) {
	    			try {
	    				String expression = state.getEvent().getEventParam(eventParamName);
		                Date eventDateTime = (Date) doJS(nodeRef, expression);

		                if ((eventDateTime != null) && (eventDateTime.getTime() <= ((Date) eventParamValue).getTime()))
			                filteredStateList.add(state);
	                } catch (ClassCastException e) {
	                	logger.error("Can't cast expression to Date. " + e.getMessage());
	                }
	    		}
	        }
    	} else
    		filteredStateList = stateList;

    	if (filteredStateList.size() == 1)
    		return filteredStateList.get(0);
    	else if (filteredStateList.size() > 1) {
    		logger.warn("Found more than one state definition for stateId=" + filteredStateList.get(0).getId()
    				+ (eventParamName != null ? " and " + eventParamName + "=" + eventParamValue : "") +
    				". The first will be returned");
    		return filteredStateList.get(0);
    	}

    	return null;
    }

    private boolean checkConditions(NodeRef nodeRef, LifeCycleTransition transition) {
    	boolean result = true;

    	List<LifeCycleCondition> conditions = transition.getConditionList();

        for (LifeCycleCondition condition : conditions) {
        	boolean conditionResult = checkCondition(nodeRef, condition);
        	result &= conditionResult;
        }

        return result;
    }

    private boolean checkCondition(NodeRef nodeRef, LifeCycleCondition condition) {
        String conditionType = condition.getType();

        if (conditionType != null) {
            boolean conditionResult = false;

            if (conditionType.equals(LifeCycleConstants.VAL_JAVASCRIPT)) {
                String code = condition.getParam(LifeCycleConstants.VAL_CODE);

                if ((code != null) && (!code.trim().isEmpty())) {
                    Object jsResult = doJS(nodeRef, code);

                    if ((jsResult != null) && (jsResult instanceof Boolean)) {
                        conditionResult = (Boolean) jsResult;
                    } else {
                        logger.error("Condition should return boolean, but instead returned " + jsResult);
                        logger.error("DocType: " + nodeService.getType(nodeRef));
                        logger.error("Condition Code: " + code);
                    }
                } else
                    conditionResult = true;
            } else {
                ActionService actionService = serviceRegistry.getActionService();
                String actionConditionName = LifeCycleHelper.getConditionEvaluatorName(conditionType);

                ActionCondition actionCondition = actionService.createActionCondition(actionConditionName);

                if (actionCondition != null) {
                    for (String paramName : condition.getParamsNames()) {
                        actionCondition.setParameterValue(paramName,
                                LifeCycleHelper.getPreparedConditionParameter(conditionType, paramName,
                                        condition.getParam(paramName), serviceRegistry));
                    }

                    conditionResult = actionService.evaluateActionCondition(actionCondition, nodeRef);
                } else {
                    logger.error("Given condition type " + conditionType + " is unsupported");
                }
            }

            return conditionResult;
        }

        return false;
    }

    private void performAction(NodeRef nodeRef, LifeCycleAction action) {
        String actionType = action.getType();

        if (actionType != null) {
            if (actionType.equals(LifeCycleConstants.VAL_JAVASCRIPT)) {
                String code = action.getParam(LifeCycleConstants.VAL_CODE);

                if (code != null)
                    doJS(nodeRef, code);
            } else {
                ActionService actionService = serviceRegistry.getActionService();
                String actionExecutorName = LifeCycleHelper.getActionExecutorName(actionType);

                Action alfrescoAction = actionService.createAction(actionExecutorName);

                if (alfrescoAction != null) {
                    for (String paramName : action.getParamsNames()) {
                        alfrescoAction.setParameterValue(paramName,
                                LifeCycleHelper.getPreparedActionParameter(actionType, paramName,
                                        action.getParam(paramName), serviceRegistry));
                    }

                    actionService.executeAction(alfrescoAction, nodeRef);
                } else {
                    logger.error("Given action type " + actionType + " is unsupported");
                }
            }
        }
    }

    private Map<String, Object> fillModel(NodeRef nodeRef) {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put("document", nodeRef);
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        NodeRef personRef = personService.personExists(userName) ? personService.getPerson(userName) : null;
        model.put("person", personRef);

        if (AlfrescoTransactionSupport.isActualTransactionActive()) {
            Map<String, Object> processVariables = AlfrescoTransactionSupport.getResource(PROCESS_VARS);

            if (processVariables != null) {
                model.put("process", processVariables);
            }
        }

        return model;
    }

    private Object doJS(NodeRef nodeRef, String script) {
        Object result = null;

        if (script != null && !script.trim().equals("")) {
            String header = "<import resource=\"classpath:/alfresco/extension/scripts/lifeCycle.lib.js\">\n\n";
            script = header + script;

            try {
                result = processor.executeString(script, fillModel(nodeRef));
            } catch (Throwable e) {
                logger.error("Lifecycle can not execute JS. nodeRef=" + nodeRef + " js=" + script, e);
                throw e;
            }
        }

        return result;
    }

    @Override
    public String getDocumentState(final NodeRef nodeRef) {
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<String>() {
            @Override
            public String doWork() throws Exception {
				if (!nodeService.hasAspect(nodeRef, LifeCycleModel.ASPECT_HAS_STATE)) {
					return null;
				}

				return (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_STATE);
			}
		});
    }

    @Override
    public List<LifeCycleTransition> getAvailableUserEvents(NodeRef nodeRef) {
        Set<String> filters = new HashSet<String>();
        filters.add(LifeCycleModel.CONSTR_USER_TRANSITION);
        filters.add(LifeCycleModel.CONSTR_TRANSITION_ON_START_PROCESS);

        return getTransitionsByEventTypesAndTrueConditions(nodeRef, filters);
    }

    @Override
    public List<LifeCycleTransition> getTransitionsByDocState(NodeRef nodeRef) {
    	String state = getDocumentState(nodeRef);
        LifeCycleDefinition lcd = getLifeCycleDefinitionByDocRef(nodeRef);

        if (lcd == null)
        	return new ArrayList<LifeCycleTransition>();

        return LifeCycleHelper.getTransitionsByFromStateId(lcd, state);
    }

    @Override
    public List<NodeRef> getDocumentsWithTimer() {
    	return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<List<NodeRef>>() {
            @Override
            public List<NodeRef> doWork() throws Exception {
		        List<NodeRef> result;

		        String query = "@" + LifeCycleModel.PROP_EVENT_TIME + ":[MIN TO NOW] OR (ASPECT:\"" + LifeCycleModel.ASPECT_HAS_TIMER +
		        "\" AND ISNULL:\"" + LifeCycleModel.PROP_EVENT_TIME + "\")";

		        ResultSet rows = null;

		        try {
		            rows = searchService.query(
		                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
		                    SearchService.LANGUAGE_FTS_ALFRESCO,
		                    query
		            );

		            result = new ArrayList<NodeRef>(rows.length());

		            for (ResultSetRow row : rows) {
		                result.add(row.getNodeRef());
		            }
		        } finally {
		            if (rows != null)
		                rows.close();
		        }

		        return result;
            }
    	});
    }

    private List<LifeCycleTransition> getTransitionsByEventTypesAndTrueConditions(NodeRef nodeRef, Set<String> filters) {
    	List<LifeCycleTransition> result = new ArrayList<LifeCycleTransition>();
    	List<LifeCycleTransition> transitionsForThisType = getTransitionsByDocStateEventTypes(nodeRef, filters);

        for (LifeCycleTransition transition : transitionsForThisType) {
            if (checkConditions(nodeRef, transition)) {
                result.add(transition);
            }
        }

        return result;
    }

    public LifeCycleDefinition getLifeCycleDefinitionByDocType(QName docType) {
    	return definitions.get(docType);
    }

    private LifeCycleDefinition getLifeCycleDefinitionByDocRef(NodeRef nodeRef) {
    	 QName docType = nodeService.getType(nodeRef);
         return getLifeCycleDefinitionByDocType(docType);
    }

    private List<LifeCycleTransition> getTransitionsByDocStateEventTypes(NodeRef nodeRef, Set<String> filters) {
        String state = getDocumentState(nodeRef);
        LifeCycleDefinition lcd = getLifeCycleDefinitionByDocRef(nodeRef);

        if (lcd == null)
        	return new ArrayList<LifeCycleTransition>();

        List<LifeCycleTransition> transitionsForThisState = LifeCycleHelper.getTransitionsByFromStateId(lcd, state);

        return LifeCycleHelper.filterTransitionsByEventType(transitionsForThisState, filters);
    }

    private List<LifeCycleState> getStateDefsByStateIdEventTypes(NodeRef nodeRef, String stateId, Set<String> eventTypes) {
    	LifeCycleDefinition lcd = getLifeCycleDefinitionByDocRef(nodeRef);

    	if (lcd == null)
        	return new ArrayList<LifeCycleState>();

    	List<LifeCycleState> statesForStateId = LifeCycleHelper.getStatesByStateId(lcd, stateId);

    	return LifeCycleHelper.filterStatesByEventType(statesForStateId, eventTypes);
    }

    @Override
    public void deployLifeCycle(InputStream lifeСycleDefinitionStream, String formatName, QName docType, String title) throws IOException {
        LifeCycleFormat format = formats.get(formatName);

        if (format == null) {
            throw new AlfrescoRuntimeException("LifeCycle format is not registered: " + formatName);
        }

        LifeCycleDefinition lcDefinition = format.parseLifeCycleDefinition(lifeСycleDefinitionStream);
        lcDefinition.setDocType(docType.toString());
        lcDefinition.setSourceFormat(formatName);
        lcDefinition.setTitle(title);
        lcDefinition.setEnabled(true);

        definitions.put(docType, lcDefinition);

        logger.info("Deployed lifecycle for " + docType + " in " + format.getName() + " format");
    }

    @Override
    public void undeployLifeCycle(QName docType) {
        if (definitions.get(docType) != null) {
            definitions.remove(docType);

            logger.info("Undeployed lifecycle for " + docType);
        }
    }

    @Override
    public void deployStoredLifeCycle(NodeRef lifeCycleDefinitionNodeRef) {
        LifeCycleDefinition lcd = getStoredLifeCycleDefinition(lifeCycleDefinitionNodeRef);

        if ((lcd != null) && (lcd.getEnabled())) {
            QName docType = (QName) nodeService.getProperty(lifeCycleDefinitionNodeRef, LifeCycleModel.PROP_DOC_TYPE);

            definitions.put(docType, lcd);
            deployedRepoDefinitions.put(docType, lifeCycleDefinitionNodeRef);

            logger.info("Deployed stored lifecycle for " + docType + " (NodeRef=" + lifeCycleDefinitionNodeRef + ")");
        }
    }

    @Override
    public void deployStoredLifeCycles() {
        List<NodeRef> repoDefinitions = getStoredLifeCycleDefinitions();

        for (NodeRef repoDefinitionNodeRef : repoDefinitions)
            deployStoredLifeCycle(repoDefinitionNodeRef);
    }

    @Override
    public void undeployStoredLifeCycle(NodeRef lifeCycleDefinitionNodeRef) {
        for (QName qName : deployedRepoDefinitions.keySet()) {
            NodeRef nodeRef = deployedRepoDefinitions.get(qName);
            if ((nodeRef != null) && (nodeRef.equals(lifeCycleDefinitionNodeRef))) {
                deployedRepoDefinitions.remove(qName);
                definitions.remove(qName);

                logger.info("Undeployed stored lifecycle for " + qName + " (NodeRef=" + lifeCycleDefinitionNodeRef + ")");
            }
        }
    }

    @Override
    public List<NodeRef> getStoredLifeCycleDefinitions() {
        NodeRef companyHome = repositoryHelper.getCompanyHome();
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(companyHome,
                Collections.singleton(LifeCycleModel.TYPE_LIFECYCLE_DEFINITION));

        List<NodeRef> repoDefinitions = new ArrayList<NodeRef>(childAssocs.size());

        for (ChildAssociationRef childAssoc : childAssocs)
            repoDefinitions.add(childAssoc.getChildRef());

        return repoDefinitions;
    }

    @Override
    public List<NodeRef> getStoredLifeCycleDefinitionsByDocType(QName docType) {
        NodeRef companyHome = repositoryHelper.getCompanyHome();
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocsByPropertyValue(companyHome, LifeCycleModel.PROP_DOC_TYPE, docType);

        List<NodeRef> repoDefinitions = new ArrayList<NodeRef>(childAssocs.size());

        for (ChildAssociationRef childAssoc : childAssocs)
            repoDefinitions.add(childAssoc.getChildRef());

        return repoDefinitions;
    }

    @Override
    public LifeCycleDefinition getStoredLifeCycleDefinition(final NodeRef nodeRef) {
        LifeCycleDefinition result = null;

        if (isNodeWithLifeCycleDefinition(nodeRef)) {
            TransactionService transactionService = serviceRegistry.getTransactionService();
            result = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<LifeCycleDefinition>(){
                public LifeCycleDefinition execute() throws Throwable {
                    String formatName = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_LIFECYCLE_FORMAT);
                    QName docType = (QName) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_DOC_TYPE);
                    Boolean enabled = (Boolean) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_LIFECYCLE_ENABLED);
                    String title = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_LIFECYCLE_TITLE);
                    ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

                    if ((formatName != null) && (contentData != null)) {
                        try {
                            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                            InputStream inputStream = reader.getContentInputStream();

                            LifeCycleFormat format = formats.get(formatName);

                            if (format != null) {
                                LifeCycleDefinition lcd = format.parseLifeCycleDefinition(inputStream);
                                lcd.setDocType(docType.toString());
                                lcd.setSourceFormat(formatName);
                                lcd.setTitle(title);
                                lcd.setEnabled(enabled);

                                return lcd;
                            } else
                                logger.error("LifeCycle format in this node is not registered. Unable to parse");
                        } catch (IOException e) {
                            logger.error("Unable to retrieve lifecycle content from given NodeRef=" + nodeRef.getId() +
                                    " due to following error: " + e.getMessage(), e);
                        }
                    } else
                        logger.error("Node with LifeCycle hasn't enought parameters to parse definition");

                    return null;
                }
            });
        } else
            logger.error("Specified NodeRef=" + nodeRef + " is not LifeCycleDefinition type");

        return result;
    }

    @Override
    public List<Map<String, Object>> getStoredLifeCycleDefinitionsHeaders(QName requiredDocType) {
        List<NodeRef> repoDefinitions = null;

        if (requiredDocType == null)
            repoDefinitions = getStoredLifeCycleDefinitions();
        else
            repoDefinitions = getStoredLifeCycleDefinitionsByDocType(requiredDocType);

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        for (NodeRef nodeRef : repoDefinitions) {
            if (nodeRef != null) {
                Map<String, Object> lcdHeaders = new HashMap<String, Object>();

                String format = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_LIFECYCLE_FORMAT);
                QName docType = (QName) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_DOC_TYPE);
                Boolean enabled = (Boolean) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_LIFECYCLE_ENABLED);
                String title = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_LIFECYCLE_TITLE);

                lcdHeaders.put("format", format);
                lcdHeaders.put("docType", docType != null ? docType.toPrefixString(serviceRegistry.getNamespaceService()) : null);
                lcdHeaders.put("enabled", enabled);
                lcdHeaders.put("title", title);
                lcdHeaders.put("nodeRef", nodeRef.toString());

                result.add(lcdHeaders);
            }
        }

        return result;
    }

    @Override
    public boolean isNodeWithLifeCycleDefinition(NodeRef nodeRef) {
        if (nodeRef != null) {
            try {
                QName nodeType = nodeService.getType(nodeRef);

                if (LifeCycleModel.TYPE_LIFECYCLE_DEFINITION.equals(nodeType))
                    return true;
            } catch (InvalidNodeRefException e) {
                logger.error("Non-existing node: " + nodeRef);
            }
        }

        return false;
    }

    @Override
    public NodeRef storeLifeCycleDefinition(final NodeRef nodeRef, final String content, final String formatName,
            final QName docType, final String title, final Boolean enabled) {

        final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

        if (formatName != null)
            properties.put(LifeCycleModel.PROP_LIFECYCLE_FORMAT, formatName);

        if (enabled != null)
            properties.put(LifeCycleModel.PROP_LIFECYCLE_ENABLED, enabled);

        if (docType != null)
            properties.put(LifeCycleModel.PROP_DOC_TYPE, docType);

        if (title != null)
            properties.put(LifeCycleModel.PROP_LIFECYCLE_TITLE, title);

        TransactionService transactionService = serviceRegistry.getTransactionService();
        NodeRef exitNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>(){
            public NodeRef execute() throws Throwable {
                if ((nodeRef == null) || (!nodeService.exists(nodeRef))) {
                    if ((content == null) || (formatName == null) || (docType == null) || (enabled == null)) {
                        logger.error("Unable to create lifecycle definition in repo because one "
                                + "or more mandatory properties missed");
                        return null;
                    }

                    NodeRef companyHome = repositoryHelper.getCompanyHome();

                    ChildAssociationRef childRecordRef = nodeService.createNode(
                            companyHome,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                    LifeCycleServiceImpl.class.getSimpleName()),
                            LifeCycleModel.TYPE_LIFECYCLE_DEFINITION,
                            properties);

                    writeContentToLifeCycleNode(childRecordRef.getChildRef(), content);

                    logger.info("LifeCycle definition successfully stored to repository. NodeRef=" + childRecordRef.getChildRef());

                    return childRecordRef.getChildRef();
                } else {
                    nodeService.setProperties(nodeRef, properties);
                    writeContentToLifeCycleNode(nodeRef, content);

                    logger.info("LifeCycle definition successfully updated in repository. NodeRef=" + nodeRef);

                    return nodeRef;
                }
            }
        });

        return exitNodeRef;
    }

    private void writeContentToLifeCycleNode(NodeRef nodeRef, String content) {
        if (content != null) {
            ContentWriter contentWriter;
            Writer writer = null;

            try {
                contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter.setMimetype(MediaType.APPLICATION_XML.toString());

                writer = new OutputStreamWriter(contentWriter.getContentOutputStream(), Charset.forName("UTF-8"));

                writer.write(content);
            } catch (IOException e) {
                logger.error("Unable to store lifecycle definition content: " + e.getMessage());
                logger.error(e.getMessage(), e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        logger.error("Failed to close writer for node " + nodeRef, e);
                    }
                }
            }
        }
    }

    public Set<QName> getDocumentTypesWithLifeCycleDefinitions() {
        List<NodeRef> repoDefinitions = getStoredLifeCycleDefinitions();
        Set<QName> docTypes = new HashSet<QName>();

        for (NodeRef repoNodeRef : repoDefinitions) {
            QName docType = (QName) nodeService.getProperty(repoNodeRef, LifeCycleModel.PROP_DOC_TYPE);
            docTypes.add(docType);
        }

        return docTypes;
    }

    public String serializeLifeCycleDefinition(LifeCycleDefinition lcd, String format) {
        LifeCycleFormat lcf = formats.get(format);
        return lcf.serializeLifeCycleDefinition(lcd);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setProcessor(ScriptProcessor processor) {
        this.processor = processor;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /*package*/ void registerFormat(String name, LifeCycleFormat format) {
        formats.put(name, format);
    }

}
