package ru.citeck.ecos.flowable.listeners.global.impl.process.end;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.StringUtils;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.ActionConditionUtils;
import ru.citeck.ecos.flowable.listeners.global.GlobalEndExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.model.CiteckWorkflowModel;
import ru.citeck.ecos.model.EcosProcessModel;
import ru.citeck.ecos.model.ICaseTaskModel;
import ru.citeck.ecos.utils.AlfActivityUtils;
import ru.citeck.ecos.utils.RepoUtils;

public class CaseTaskEndProcessListener implements GlobalEndExecutionListener {

    private NodeService nodeService;
    private CaseActivityService caseActivityService;
    private AlfActivityUtils alfActivityUtils;

    @Override
    public void notify(DelegateExecution delegateExecution) {
        AuthenticationUtil.runAsSystem(() -> {
            if (FlowableListenerUtils.getDocument(delegateExecution, nodeService) == null) {
                return null;
            }
            stopActivity(delegateExecution);
            return null;
        });
    }

    private void stopActivity(DelegateExecution delegateExecution) {
        NodeRef bpmPackage = FlowableListenerUtils.getWorkflowPackage(delegateExecution);
        nodeService.setProperty(bpmPackage, CiteckWorkflowModel.PROP_IS_WORKFLOW_ACTIVE, false);

        NodeRef taskActivityNodeRef = RepoUtils.getFirstSourceAssoc(bpmPackage,
                ICaseTaskModel.ASSOC_WORKFLOW_PACKAGE, nodeService);
        if (taskActivityNodeRef != null) {
            ActionConditionUtils.getProcessVariables().putAll(delegateExecution.getVariables());

            ActivityRef taskActivityRef = alfActivityUtils.composeActivityRef(taskActivityNodeRef);
            caseActivityService.stopActivity(taskActivityRef);
        }

        String rawActivityRef = (String) nodeService.getProperty(bpmPackage, EcosProcessModel.PROP_ACTIVITY_REF);
        if (StringUtils.isNotBlank(rawActivityRef)) {
            ActionConditionUtils.getProcessVariables().putAll(delegateExecution.getVariables());

            ActivityRef taskActivityRef = ActivityRef.of(rawActivityRef);
            caseActivityService.stopActivity(taskActivityRef);
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    @Autowired
    public void setAlfActivityUtils(AlfActivityUtils alfActivityUtils) {
        this.alfActivityUtils = alfActivityUtils;
    }
}
