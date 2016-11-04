package ru.citeck.ecos.action;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition;
import ru.citeck.ecos.lifecycle.LifeCycleService;
import ru.citeck.ecos.action.NodeActionDefinition.NodeActionType;
import ru.citeck.ecos.action.NodeActionDefinition.URLContext;

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
            NodeActionDefinition action = new NodeActionDefinition();
            if ("onStartProcess".equals(eventType)) {
                String form = actionParams.get("formId");
                form = form==null ? "" : "&formId=" + form;
                url = String.format(URL_REDIRECT_TEMPLATE, nodeRef.toString(), actionParams.get("workflowId"), form);
                action.setActionType(NodeActionType.REDIRECT);
            } else if ("userTransition".equals(eventType)) {
                if (actionParams.get("urlId") != null) {
                    url = actionParams.get("urlId").replace("{nodeRef}", nodeRef.toString());
                    action.setContext(URLContext.URL_SERVICECONTEXT);
                    action.setActionType(NodeActionType.REDIRECT);
                } else {
                    url = String.format(URL_SERVER_ACTION_TEMPLATE, nodeRef.toString());
                }
            } else {
                throw new RuntimeException("Unsupported lifecycle event type: " + eventType);
            }
            action.setTitle("lifecycle.action." + actionParams.get("actionName"));
            action.setUrl(url);
            actionDefinitionList.add(action);
        }
        return actionDefinitionList;
    }

    public void setLifecycleService(LifeCycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }
}
