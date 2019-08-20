package ru.citeck.ecos.workflow.activiti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.workflow.EcosWorkflowService;
import ru.citeck.ecos.workflow.EngineWorkflowService;

import java.util.List;

@Component
public class ActivitiEngineWorkflowService implements EngineWorkflowService {

    public static final Logger logger = LoggerFactory.getLogger(ActivitiEngineWorkflowService.class);

    @Autowired
    public ActivitiEngineWorkflowService(EcosWorkflowService workflowService) {
        workflowService.register("activiti", this);
    }

    public void sendSignal(List<String> processes, String signalName) {
        logger.error("Signal sending is not implemented. Skip it. Processes: " + processes + " signal: " + signalName);
    }
}
