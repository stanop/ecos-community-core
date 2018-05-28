package ru.citeck.ecos.workflow.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.workflow.listeners.AbstractTaskListener;
import ru.citeck.ecos.workflow.listeners.ListenerUtils;

import java.util.Objects;

/**
 * @author Andrey Platunov on 25.04.2018
 */
public class SetLastConfirmOutcomeToOrdersListener extends AbstractTaskListener {

    private QName TYPE_CONFIRM_TASK = QName.createQName("http://www.citeck.ru/model/workflow/confirm/1.0", "confirmTask");

    private NodeService nodeService;
    private NamespaceService namespaceService;

    private QName fieldToSet;
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

        QName taskQName = QName.createQName(delegateTask.getFormKey(), namespaceService);
        if (!Objects.equals(taskQName, TYPE_CONFIRM_TASK)) {
            return;
        }

        String currentTaskOutcome = (String) delegateTask.getVariable("bpm_outcome");

        if (StringUtils.isNotEmpty(currentTaskOutcome)) {
            nodeService.setProperty(document, fieldToSet, currentTaskOutcome);
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setFieldToSet(QName fieldToSet) {
        this.fieldToSet = fieldToSet;
    }

    public void setRequiredDocType(QName requiredDocType) {
        this.requiredDocType = requiredDocType;
    }
}
