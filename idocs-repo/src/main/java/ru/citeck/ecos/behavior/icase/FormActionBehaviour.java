package ru.citeck.ecos.behavior.icase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.behavior.JavaBehaviour;
import ru.citeck.ecos.form.action.FormActionHandlerProvider;
import ru.citeck.ecos.form.action.handlers.FormActionHandler;
import ru.citeck.ecos.model.IdocsModel;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class FormActionBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static final Log logger = LogFactory.getLog(FormActionBehaviour.class);

    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private FormActionHandlerProvider formActionHandlerProvider;
    private ObjectMapper objectMapper;

    public void init() {
        objectMapper = new ObjectMapper();
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                IdocsModel.ASPECT_HAS_CUSTOM_FORM_ACTION,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!checkProperties(nodeRef, before, after)) {
            return;
        }

        FormActionData formActionData = deserializeActionDataProperty(after);
        if (formActionData == null) {
            return;
        }

        List<FormActionHandler> handlersByTaskType =
                formActionHandlerProvider.getHandlersByTaskType(formActionData.taskType);
        for (FormActionHandler handler : handlersByTaskType) {
            handler.handle(nodeRef, formActionData.getOutcome());
        }

        if (nodeService.exists(nodeRef)) {
            nodeService.setProperty(nodeRef, IdocsModel.PROP_CUSTOM_FORM_ACTION_DATA, null);
        }
    }

    private FormActionData deserializeActionDataProperty(Map<QName, Serializable> after) {
        try {
            String jsonFormData = (String) after.get(IdocsModel.PROP_CUSTOM_FORM_ACTION_DATA);
            return objectMapper.readValue(jsonFormData, FormActionData.class);
        } catch (IOException e) {
            logger.error(e);
            return null;
        }
    }

    private boolean checkProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!nodeService.exists(nodeRef)) {
            return false;
        }

        Serializable customActionBefore = before.get(IdocsModel.PROP_CUSTOM_FORM_ACTION_DATA);
        Serializable customActionAfter = after.get(IdocsModel.PROP_CUSTOM_FORM_ACTION_DATA);
        if (customActionAfter == null || customActionAfter.equals(customActionBefore)) {
            return false;
        }

        String formAction = (String) customActionAfter;
        if (formAction.isEmpty()) {
            return false;
        }

        return true;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setFormActionHandlerProvider(FormActionHandlerProvider formActionHandlerProvider) {
        this.formActionHandlerProvider = formActionHandlerProvider;
    }


    private static class FormActionData {

        private String taskType;
        private String outcome;

        public String getTaskType() {
            return taskType;
        }

        public void setTaskType(String taskType) {
            this.taskType = taskType;
        }

        public String getOutcome() {
            return outcome;
        }

        public void setOutcome(String outcome) {
            this.outcome = outcome;
        }

    }
}