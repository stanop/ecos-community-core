package ru.citeck.ecos.webscripts.tasks;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.repository.NodeRef;
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
import ru.citeck.ecos.workflow.listeners.GrantWorkflowTaskPermissionExecutor;
import ru.citeck.ecos.workflow.mirror.WorkflowMirrorService;

import java.io.Serializable;
import java.util.*;

public class ChangeTaskOwner extends AbstractWorkflowWebscript implements ApplicationContextAware {

    private DeputyService deputyService;
    private ApplicationContext applicationContext;
    private String delegateListenerName;
    private WorkflowMirrorService workflowMirrorService;
    private GrantWorkflowTaskPermissionExecutor grantWorkflowTaskPermissionExecutor;

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
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new Exception("Unrecognized parameter value. Parameter " + ACTION_PARAM + " is expected to be either claim or release");
            }

            WorkflowTask workflowTask = workflowService.getTaskById(taskId);
            String claimOwner = (String) workflowTask.getProperties().get(QName.createQName(null, CLAIM_OWNER));
            if ("null".equals(claimOwner)) claimOwner = null;


            List<String> assistants = new ArrayList<>();
            if (!(action == Action.RELEASE && claimOwner == null)) {
                assistants.addAll(deputyService.getUserAssistants(action == Action.CLAIM ? owner : claimOwner));
            }

            boolean hasAssistants = assistants.size() > 0;
            if (hasAssistants) {
                assistants.add(action == Action.CLAIM ? owner : claimOwner);
                TaskDeputyListener delegateListener = applicationContext.getBean(delegateListenerName, TaskDeputyListener.class);
                delegateListener.updatePooledActors(Collections.singletonList(workflowTask), assistants, action == Action.CLAIM);
            }

            Map<QName, Serializable> props = setOwners(action, owner, hasAssistants);
            workflowTask = updateTaskAsSystem(workflowTask.getId(), props, null, null);

            if (hasAssistants) {
                workflowMirrorService.mirrorTask(workflowTask);
                grantWorkflowTaskPermissionExecutor.grantPermissions(workflowTask);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("workflowTask", modelBuilder.buildDetailed(workflowTask));
            return model;
        } catch (JSONException e) {
            throw new WebScriptException(400, "Could not parse JSON from request.", e);
        } catch (Exception e) {
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

    public void setWorkflowMirrorService(WorkflowMirrorService workflowMirrorService) {
        this.workflowMirrorService = workflowMirrorService;
    }

    public void setGrantWorkflowTaskPermissionExecutor(GrantWorkflowTaskPermissionExecutor grantWorkflowTaskPermissionExecutor) {
        this.grantWorkflowTaskPermissionExecutor = grantWorkflowTaskPermissionExecutor;
    }

    private Map<QName, Serializable> setOwners(Action action, String owner, boolean hasAssistants) {
        Serializable setOwner = (action == Action.CLAIM && !hasAssistants) ? owner : null;
        Serializable setClaimOwner = (action == Action.CLAIM) ? owner : null;

        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_OWNER, setOwner);
        props.put(QName.createQName(null, "claimOwner"), setClaimOwner);

        return props;
    }

    private WorkflowTask updateTaskAsSystem(String taskId, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add, Map<QName, List<NodeRef>> remove) {
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<WorkflowTask>() {
            public WorkflowTask doWork() throws Exception {
                WorkflowTask workflowTask = workflowService.updateTask(taskId, properties, add, remove);
                return workflowTask;
            }
        });
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