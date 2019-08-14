package ru.citeck.ecos.action;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.workflow.EcosWorkflowService;

import java.util.List;

public class SendWorkflowSignalActionExecutor extends ActionExecuterAbstractBase {

    private static final Logger logger = LoggerFactory.getLogger(SendWorkflowSignalActionExecutor.class);

    public static final String NAME = "send-workflow-signal";
    public static final String PARAM_SIGNAL_NAME = "signalName";

    private EcosWorkflowService ecosWorkflowService;

    @Autowired
    public SendWorkflowSignalActionExecutor(EcosWorkflowService ecosWorkflowService) {
        this.ecosWorkflowService = ecosWorkflowService;
    }

    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        String signalName = (String) action.getParameterValue(PARAM_SIGNAL_NAME);

        if (StringUtils.isBlank(signalName)) {
            logger.error(PARAM_SIGNAL_NAME + " is a mandatory parameter");
        }

        ecosWorkflowService.sendSignal(actionedUponNodeRef, signalName);
    }

    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(
                PARAM_SIGNAL_NAME,
                DataTypeDefinition.TEXT,
                true,
                getParamDisplayLabel(PARAM_SIGNAL_NAME)
        ));
    }
}
