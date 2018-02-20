package ru.citeck.ecos.flowable.listeners.global.impl.task.complete;

import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.flowable.task.service.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalCompleteTaskListener;
import ru.citeck.ecos.notification.AbstractNotificationSender;

import java.util.Map;

/**
 * Complete task listener
 */
public class CompleteTaskListener implements GlobalCompleteTaskListener {

    /**
     * Constants
     */
    private static final String ENGINE_PREFIX = "flowable$";

    /**
     * Services
     */
    private AbstractNotificationSender<DelegateTask> sender;
    protected NodeService nodeService;
    private Map<String, Map<String,String>> conditions;
    protected NamespaceService namespaceService;
    private WorkflowService workflowService;
    protected WorkflowQNameConverter qNameConverter;

    /**
     * Is enabled
     */
    protected boolean enabled;

    /**
     * Init
     */
    public void init() {
        qNameConverter = new WorkflowQNameConverter(namespaceService);
    }

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        if(enabled) {
            Boolean value = (Boolean) delegateTask.getVariable("cwf_sendNotification");
            if (Boolean.TRUE.equals(value)) {
                WorkflowTask wfTask = workflowService.getTaskById(ENGINE_PREFIX + delegateTask.getId());
                if (conditions != null && wfTask != null) {
                    Map<String, String> condition = conditions.get(wfTask.getName());
                    int result = 0;
                    if (condition != null && condition.size() > 0) {
                        for (Map.Entry<String, String> entry : condition.entrySet()) {
                            String actualValue = (String) wfTask.getProperties().get(qNameConverter.mapNameToQName(entry.getKey()));
                            if (!actualValue.equals(entry.getValue())) {
                                result++;
                            }
                        }
                    }
                    if (result == 0) {
                        sender.sendNotification(delegateTask);
                    }
                } else {
                    sender.sendNotification(delegateTask);
                }
            }
        }
    }

    /** Setters */

    public void setSender(AbstractNotificationSender<DelegateTask> sender) {
        this.sender = sender;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
