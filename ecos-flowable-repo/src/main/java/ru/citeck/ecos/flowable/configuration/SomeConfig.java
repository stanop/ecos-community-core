package ru.citeck.ecos.flowable.configuration;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.flowable.engine.impl.jobexecutor.AsyncTriggerJobHandler;
import org.flowable.engine.impl.jobexecutor.TriggerTimerEventJobHandler;
import org.flowable.job.service.JobHandler;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.variable.api.types.VariableType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.flowable.jobexecutor.AuthenticatedAsyncJobHandler;
import ru.citeck.ecos.flowable.jobexecutor.AuthenticatedTimerJobHandler;
import ru.citeck.ecos.flowable.variable.FlowableEcosPojoTypeHandler;
import ru.citeck.ecos.flowable.variable.FlowableScriptNodeListVariableType;
import ru.citeck.ecos.flowable.variable.FlowableScriptNodeVariableType;
import ru.citeck.ecos.workflow.variable.handler.EcosPojoTypeHandler;

import java.util.ArrayList;
import java.util.List;


@Component
public class SomeConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    private NodeService nodeService;
    private EcosPojoTypeHandler<?> ecosPojoTypeHandler;
    private FlowableScriptNodeVariableType flowableScriptNodeVariableType;
    private FlowableScriptNodeListVariableType flowableScriptNodeListVariableType;

    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        System.out.println("+++++++++++++++++++23423423423+++++++++++++++++++++++++++++++++++");



        List<VariableType> types = engineConfiguration.getCustomPreVariableTypes();
        types = types != null ? new ArrayList<>(types) : new ArrayList<>();
        types.add(new FlowableEcosPojoTypeHandler(ecosPojoTypeHandler));
        types.add(flowableScriptNodeVariableType);
        types.add(flowableScriptNodeListVariableType);
        engineConfiguration.setCustomPreVariableTypes(types);


        List<JobHandler> customJobHandlers = engineConfiguration.getCustomJobHandlers();
        customJobHandlers = customJobHandlers != null ? new ArrayList<>(customJobHandlers) : new ArrayList<>();

        AsyncContinuationJobHandler asyncContinuationJobHandler = new AsyncContinuationJobHandler();
        customJobHandlers.add(new AuthenticatedAsyncJobHandler(asyncContinuationJobHandler));

        AsyncTriggerJobHandler asyncTriggerJobHandler = new AsyncTriggerJobHandler();
        customJobHandlers.add(new AuthenticatedAsyncJobHandler(asyncTriggerJobHandler));

        TriggerTimerEventJobHandler triggerTimerEventJobHandler = new TriggerTimerEventJobHandler();
        customJobHandlers.add(new AuthenticatedTimerJobHandler(triggerTimerEventJobHandler, nodeService));

        engineConfiguration.setCustomJobHandlers(customJobHandlers);


        System.out.println("+++++++++++++++++++234234+++++++++++++++++++++++++++++++++++");
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
