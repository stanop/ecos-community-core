package ru.citeck.ecos.action;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.action.node.*;
import ru.citeck.ecos.action.node.NodeActionDefinition;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition;
import ru.citeck.ecos.lifecycle.LifeCycleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author deathNC
 */
public class LifecycleActionProvider extends NodeActionsProvider {

    private static final String URL_REDIRECT_TEMPLATE = "start-specified-workflow?" +
            "packageItems=%s" + // packageItems (document nodeRef)
            "&workflowId=%s" + // workflowId
            "%s"; // formId

    private static final String URL_SERVER_ACTION_TEMPLATE = "api/lifecycle/do-transition?nodeRef=%s";
    private static final String HIDDEN_ACTION_ID = "server-hidden-action";

    private static final String PARAM_FORM_ID = "formId";
    private static final String PARAM_URL_ID = "urlId";
    private static final String PARAM_ACTION_NAME = "actionName";
    private static final String PARAM_WORKFLOW_ID = "workflowId";

    private LifeCycleService lifecycleService;

    public LifecycleActionProvider() {
    }

    @Override
    public List<NodeActionDefinition> getNodeActions(NodeRef nodeRef) {
        List<LifeCycleDefinition.LifeCycleTransition> transitionList = lifecycleService.getAvailableUserEvents(nodeRef);
        List<NodeActionDefinition> actionDefinitionList = new ArrayList<>(transitionList.size());
        for (LifeCycleDefinition.LifeCycleTransition transition : transitionList) {
            LifeCycleDefinition.LifeCycleEvent event = transition.getEvent();
            String eventType = event.getEventType();
            Map<String, String> actionParams = event.getEventParams();
            String url;
            URLAction action;

            if (HIDDEN_ACTION_ID.equals(actionParams.get(PARAM_ACTION_NAME))) {
                continue;
            }

            if ("onStartProcess".equals(eventType)) {
                action = new RedirectAction();
                String form = actionParams.get(PARAM_FORM_ID);
                form = form == null ? "" : "&formId=" + form;
                url = String.format(URL_REDIRECT_TEMPLATE, nodeRef.toString(), actionParams.get(PARAM_WORKFLOW_ID), form);
            } else if ("userTransition".equals(eventType)) {
                if (actionParams.get(PARAM_URL_ID) != null) {
                    action = new RedirectAction();
                    url = actionParams.get(PARAM_URL_ID).replace("{nodeRef}", nodeRef.toString());
                    action.setContext(URLAction.URLContext.URL_SERVICECONTEXT);
                } else {
                    action = new RequestAction();
                    url = String.format(URL_SERVER_ACTION_TEMPLATE, nodeRef.toString());
                }
            } else {
                throw new RuntimeException("Unsupported lifecycle event type: " + eventType);
            }
            action.setTitle(getTitle(actionParams.get(PARAM_ACTION_NAME)));
            action.setUrl(url);
            actionDefinitionList.add(action);
        }
        return actionDefinitionList;
    }

    private String getTitle(String actionName) {
        String key = "lifecycle.action." + actionName;
        String title = I18NUtil.getMessage(key);
        return title != null ? title : actionName;
    }

    public void setLifecycleService(LifeCycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }
}
