package ru.citeck.ecos.workflow.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.model.ConfirmWorkflowModel;
import ru.citeck.ecos.model.OrdersModel;
import ru.citeck.ecos.workflow.listeners.AbstractTaskListener;
import ru.citeck.ecos.workflow.listeners.ListenerUtils;

import java.util.Objects;

public class ResetCorrectionOutcomeOrdersTaskListener extends AbstractTaskListener {

    private static final String EVENT_NAME = "assignment";

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private NodeService nodeService;

    private String taskTypeName = null;

    @Override
    protected void notifyImpl(DelegateTask task) {
        if (!getTaskTypeName().equals(task.getFormKey()) || !EVENT_NAME.equals(task.getEventName())) {
            return;
        }

        NodeRef document = ListenerUtils.getDocument(task.getExecution(), nodeService);
        if (document == null || !nodeService.exists(document)) {
            return;
        }

        QName docType = nodeService.getType(document);
        if (!Objects.equals(docType, OrdersModel.TYPE_INTERNAL)) {
            return;
        }

        nodeService.removeProperty(document, OrdersModel.PROP_LAST_CORRECT_OUTCOME);
    }

    private String getTaskTypeName() {
        if (taskTypeName == null) {
            taskTypeName = ConfirmWorkflowModel.TYPE_CONFIRM_TASK.toPrefixString(namespaceService);
        }
        return taskTypeName;
    }
}
