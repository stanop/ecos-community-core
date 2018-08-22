package ru.citeck.ecos.workflow.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.model.ConfirmWorkflowModel;
import ru.citeck.ecos.workflow.listeners.AbstractTaskListener;
import ru.citeck.ecos.workflow.listeners.ListenerUtils;

import java.util.Objects;

/**
 * @author Andrey Platunov on 25.04.2018
 */
public class SetLastTasksOutcomeToOrdersListener extends AbstractTaskListener {

    private NodeService nodeService;
    private NamespaceService namespaceService;

    private QName fieldToSetConfirmOutcome;
    private QName fieldToSetCorrectOutcome;
    private QName requiredDocType;

    @Override
    protected void notifyImpl(DelegateTask delegateTask) {

        NodeRef document = ListenerUtils.getDocument(delegateTask, nodeService);
        if (document == null || !nodeService.exists(document)) {
            return;
        }

        QName currentDocType = nodeService.getType(document);
        if (!Objects.equals(currentDocType, requiredDocType)) {
            return;
        }

        String currentTaskOutcome = (String) delegateTask.getVariable("bpm_outcome");
        if (StringUtils.isBlank(currentTaskOutcome)) {
            return;
        }

        QName taskQName = QName.createQName(delegateTask.getFormKey(), namespaceService);
        if (Objects.equals(taskQName, ConfirmWorkflowModel.TYPE_CONFIRM_TASK)) {
            nodeService.setProperty(document, fieldToSetConfirmOutcome, currentTaskOutcome);
        }

        if (Objects.equals(taskQName, ConfirmWorkflowModel.TYPE_CORRECT_TASK)) {
            nodeService.setProperty(document, fieldToSetCorrectOutcome, currentTaskOutcome);
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setFieldToSetConfirmOutcome(QName fieldToSetConfirmOutcome) {
        this.fieldToSetConfirmOutcome = fieldToSetConfirmOutcome;
    }

    public void setFieldToSetCorrectOutcome(QName fieldToSetCorrectOutcome) {
        this.fieldToSetCorrectOutcome = fieldToSetCorrectOutcome;
    }

    public void setRequiredDocType(QName requiredDocType) {
        this.requiredDocType = requiredDocType;
    }
}
