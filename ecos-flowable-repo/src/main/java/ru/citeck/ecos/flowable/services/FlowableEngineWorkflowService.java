package ru.citeck.ecos.flowable.services;

import org.flowable.common.engine.impl.interceptor.CommandExecutor;
import org.flowable.common.engine.impl.service.CommonEngineServiceImpl;
import org.flowable.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.flowable.cmd.SendWorkflowSignalCmd;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.workflow.EcosWorkflowService;
import ru.citeck.ecos.workflow.EngineWorkflowService;

import java.util.List;

@Service
public class FlowableEngineWorkflowService implements EngineWorkflowService {

    public static final Logger logger = LoggerFactory.getLogger(FlowableEngineWorkflowService.class);

    private CommandExecutor commandExecutor;

    @Autowired
    public FlowableEngineWorkflowService(RuntimeService runtimeService,
                                         EcosWorkflowService ecosWorkflowService) {
        if (runtimeService instanceof CommonEngineServiceImpl) {
            commandExecutor = ((CommonEngineServiceImpl) runtimeService).getCommandExecutor();
        }
        ecosWorkflowService.register(FlowableConstants.ENGINE_ID, this);
    }

    @Override
    public void sendSignal(List<String> processes, String signalName) {
        if (commandExecutor == null) {
            logger.error("Command executor is null!");
            return;
        }
        commandExecutor.execute(new SendWorkflowSignalCmd(processes, signalName, false));
    }
}
