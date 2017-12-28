package ru.citeck.ecos.flowable.services;

import org.flowable.engine.repository.ProcessDefinition;

import java.io.InputStream;
import java.util.List;

/**
 * Flowable process definition service interface
 */
public interface FlowableProcessDefinitionService {

    /**
     * Is process definition deployed
     * @param inputStream Process definition input stream
     * @return Check result
     */
    boolean isProcessDefinitionDeployed(InputStream inputStream);

    /**
     * Get process definition image stream
     * @param processDefinitionId Process definition id
     * @return Process definition image stream
     */
    InputStream getProcessDefinitionImage(String processDefinitionId);

    /**
     * Get process definition by od
     * @param processDefinitionId Process definition id
     * @return Process definition
     */
    ProcessDefinition getProcessDefinitionById(String processDefinitionId);

    /**
     * Get process definition by key
     * @param definitionKey Process Definition key
     * @return Process definition
     */
    ProcessDefinition getProcessDefinitionByKey(String definitionKey);

    /**
     * Get process definition by deployment id
     * @param deploymentId Deployment id
     * @return Process definition
     */
    ProcessDefinition getProcessDefinitionByDeploymentId(String deploymentId);

    /**
     * Get process definition by process instance id
     * @param processInstanceId Process instance id
     * @return Process definition
     */
    ProcessDefinition getProcessDefinitionByProcessInstanceId(String processInstanceId);

    /**
     * Get all process definitions
     * @return List of process definitions
     */
    List<ProcessDefinition> getAllProcessDefinitions();

    /**
     * Get all last process definitions
     * @return List of process definitions
     */
    List<ProcessDefinition> getAllLastProcessDefinitions();
    /**
     * Get all process definitions by key
     * @param definitionKey Process definition key
     * @return List of process definitions
     */
    List<ProcessDefinition> getAllProcessDefinitionsByKey(String definitionKey);
}
