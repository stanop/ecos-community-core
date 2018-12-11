package ru.citeck.ecos.flowable.configuration;

import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.cfg.AbstractProcessEngineConfigurator;
import org.flowable.engine.common.impl.AbstractEngineConfiguration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.flowable.engine.impl.jobexecutor.AsyncTriggerJobHandler;
import org.flowable.engine.impl.jobexecutor.TriggerTimerEventJobHandler;
import org.flowable.job.service.JobHandler;
import org.flowable.variable.api.types.VariableType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.flowable.jobexecutor.AuthenticatedAsyncJobHandler;
import ru.citeck.ecos.flowable.jobexecutor.AuthenticatedTimerJobHandler;
import ru.citeck.ecos.flowable.variable.FlowableEcosPojoTypeHandler;
import ru.citeck.ecos.flowable.variable.FlowableScriptNodeListVariableType;
import ru.citeck.ecos.flowable.variable.FlowableScriptNodeVariableType;
import ru.citeck.ecos.workflow.variable.handler.EcosPojoTypeHandler;

import java.util.ArrayList;
import java.util.List;

@Component("ecosFlowableConfigurator")
public class EcosFlowableConfigurator extends AbstractProcessEngineConfigurator {

    private NodeService nodeService;
    private EcosPojoTypeHandler<?> ecosPojoTypeHandler;
    private FlowableScriptNodeVariableType flowableScriptNodeVariableType;
    private FlowableScriptNodeListVariableType flowableScriptNodeListVariableType;

    @Override
    public void beforeInit(AbstractEngineConfiguration engineConfiguration) {
        super.beforeInit(engineConfiguration);
        System.out.println("_____________________BEFORE INIT_____________");
    }

    @Override
    public void configure(AbstractEngineConfiguration engineConfiguration) {
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");



        ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) engineConfiguration;


        List<VariableType> types = processEngineConfiguration.getCustomPreVariableTypes();
        types = types != null ? new ArrayList<>(types) : new ArrayList<>();
        types.add(new FlowableEcosPojoTypeHandler(ecosPojoTypeHandler));
        types.add(flowableScriptNodeVariableType);
        types.add(flowableScriptNodeListVariableType);
        processEngineConfiguration.setCustomPreVariableTypes(types);


        List<JobHandler> customJobHandlers = processEngineConfiguration.getCustomJobHandlers();
        customJobHandlers = customJobHandlers != null ? new ArrayList<>(customJobHandlers) : new ArrayList<>();

        AsyncContinuationJobHandler asyncContinuationJobHandler = new AsyncContinuationJobHandler();
        customJobHandlers.add(new AuthenticatedAsyncJobHandler(asyncContinuationJobHandler));

        AsyncTriggerJobHandler asyncTriggerJobHandler = new AsyncTriggerJobHandler();
        customJobHandlers.add(new AuthenticatedAsyncJobHandler(asyncTriggerJobHandler));

        TriggerTimerEventJobHandler triggerTimerEventJobHandler = new TriggerTimerEventJobHandler();
        customJobHandlers.add(new AuthenticatedTimerJobHandler(triggerTimerEventJobHandler, nodeService));

        processEngineConfiguration.setCustomJobHandlers(customJobHandlers);


        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Autowired
    @Qualifier("nodeService")
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    @Qualifier("workflow.variable.EcosPojoTypeHandler")
    public void setEcosPojoTypeHandler(EcosPojoTypeHandler<?> ecosPojoTypeHandler) {
        this.ecosPojoTypeHandler = ecosPojoTypeHandler;
    }

    @Autowired
    @Qualifier("flowableScriptNodeType")
    public void setFlowableScriptNodeVariableType(FlowableScriptNodeVariableType flowableScriptNodeVariableType) {
        this.flowableScriptNodeVariableType = flowableScriptNodeVariableType;
    }

    @Autowired
    @Qualifier("flowableScriptNodeListType")
    public void setFlowableScriptNodeListVariableType(FlowableScriptNodeListVariableType flowableScriptNodeListVariableType) {
        this.flowableScriptNodeListVariableType = flowableScriptNodeListVariableType;
    }
}
