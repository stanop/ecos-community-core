package ru.citeck.ecos.webscripts.tasks;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.deputy.DeputyService;
import ru.citeck.ecos.deputy.TaskDeputyListener;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeTaskOwner extends AbstractWorkflowWebscript implements ApplicationContextAware {

    private DeputyService deputyService;
    private ApplicationContext applicationContext;
    private String delegateListenerName;

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest request, Status status, Cache cache) {

        String taskId = request.getServiceMatch().getTemplateVars().get(TASK_ID_PARAM);

        try {
            @SuppressWarnings("unchecked")
            JSONObject parameters = (JSONObject) request.parseContent();
            String owner = parameters.getString(CM_OWNER_PARAM);
            if ("null".equals(owner)) owner = null;

            Action action;
            try {
                action = Action.valueOf(parameters.getString(ACTION_PARAM).toUpperCase());
            }
            catch (IllegalArgumentException | NullPointerException e) {
                throw new Exception("Unrecognized parameter value. Parameter " + ACTION_PARAM + " is expected to be either claim or release");
            }

            WorkflowTask workflowTask = workflowService.getTaskById(taskId);
            String claimOwner = (String) workflowTask.getProperties().get(QName.createQName(null, CLAIM_OWNER));
            if ("null".equals(claimOwner)) claimOwner = null;

            List<String> assistants = deputyService.getUserAssistants(action == Action.CLAIM ? owner : claimOwner);
            boolean hasAssistants = assistants.size() > 0;
            if (hasAssistants) {
                TaskDeputyListener delegateListener = applicationContext.getBean(delegateListenerName, TaskDeputyListener.class);
                delegateListener.updatePooledActors(Collections.singletonList(workflowTask), assistants, action == Action.CLAIM);
            }

            Map<QName, Serializable> props = setOwners(action, owner, hasAssistants);
            workflowTask = workflowService.updateTask(workflowTask.getId(), props, null, null);

            Map<String, Object> model = new HashMap<>();
            model.put("workflowTask", modelBuilder.buildDetailed(workflowTask));
            return model;
        }
        catch (JSONException e) {
            throw new WebScriptException(400, "Could not parse JSON from request.", e);
        }
        catch (Exception e) {
            throw new WebScriptException(400, e.getMessage(), e);
        }
    }

    public void setDeputyService(DeputyService deputyService) {
        this.deputyService = deputyService;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setDelegateListenerName(String delegateListenerName) {
        this.delegateListenerName = delegateListenerName;
    }

    private Map<QName, Serializable> setOwners(Action action, String owner, boolean hasAssistants) {
        Serializable setOwner = (action == Action.CLAIM && hasAssistants) ? null : owner;
        Serializable setClaimOwner = (action == Action.CLAIM) ? owner : null;

        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_OWNER, setOwner);
        props.put(QName.createQName(null, "claimOwner"), setClaimOwner);

        return props;
    }

    private static final String CM_OWNER_PARAM = "cm_owner";
    private static final String CLAIM_OWNER = "claimOwner";
    private static final String TASK_ID_PARAM = "taskId";
    private static final String ACTION_PARAM = "action";

    private enum Action {
        CLAIM("claim"),
        RELEASE("release");

        private final String action;

        Action(final String action) {
            this.action = action;
        }

        public String toString() {
            return action;
        }
    }
}
