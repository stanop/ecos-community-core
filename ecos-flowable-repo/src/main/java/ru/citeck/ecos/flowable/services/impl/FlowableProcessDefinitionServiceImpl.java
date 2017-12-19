package ru.citeck.ecos.flowable.services.impl;

import org.apache.xerces.parsers.DOMParser;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.citeck.ecos.flowable.services.FlowableProcessDefinitionService;
import ru.citeck.ecos.flowable.services.FlowableProcessInstanceService;

import java.io.InputStream;
import java.util.List;

/**
 * Flowable process definition service
 */
public class FlowableProcessDefinitionServiceImpl implements FlowableProcessDefinitionService {

    /**
     * Repository service
     */
    private RepositoryService repositoryService;

    /**
     * Flowable process instance service
     */
    private FlowableProcessInstanceService flowableProcessInstanceService;

    /**
     * Set flowable process instance service
     * @param flowableProcessInstanceService Flowable process instance service
     */
    public void setFlowableProcessInstanceService(FlowableProcessInstanceService flowableProcessInstanceService) {
        this.flowableProcessInstanceService = flowableProcessInstanceService;
    }

    /**
     * Set repository service
     * @param repositoryService Repository service
     */
    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    /**
     * Is process definition deployed
     * @param inputStream Process definition input stream
     * @return Check result
     */
    @Override
    public boolean isProcessDefinitionDeployed(InputStream inputStream) {
        try {
            String processDefinitionKey = getProcessDefinitionKey(inputStream);
            if (processDefinitionKey != null) {
                return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).count() > 0;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get process definition id (key)
     * @param workflowDefinition Workflow definition input stream
     * @return Process definition id
     * @throws Exception
     */
    private String getProcessDefinitionKey(InputStream workflowDefinition) throws Exception {
        try {
            InputSource inputSource = new InputSource(workflowDefinition);
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document document = parser.getDocument();
            /** Get processes dom-elements */
            NodeList elements = document.getElementsByTagName("process");
            if (elements.getLength() < 1) {
                throw new IllegalArgumentException("The input stream does not contain a process definition!");
            }
            /** Get id element */
            NamedNodeMap attributes = elements.item(0).getAttributes();
            Node idAttrib = attributes.getNamedItem("id");
            if (idAttrib == null) {
                throw new IllegalAccessError("The process definition does not have an id!");
            }
            return idAttrib.getNodeValue();
        }
        finally {
            workflowDefinition.close();
        }
    }


    /**
     * Get process definition image stream
     *
     * @param processDefinitionId Process definition id
     * @return Process definition image stream
     */
    @Override
    public InputStream getProcessDefinitionImage(String processDefinitionId) {
        ProcessDefinition processDefinition = getProcessDefinitionById(processDefinitionId);
        if (processDefinition != null) {
            return repositoryService.getProcessDiagram(processDefinitionId);
        } else {
            return null;
        }
    }

    /**
     * Get process definition by od
     * @param processDefinitionId Process definition id
     * @return Process definition
     */
    @Override
    public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        return repositoryService.getProcessDefinition(processDefinitionId);
    }

    /**
     * Get process definition by key
     * @param definitionKey Process Definition key
     * @return Process definition
     */
    @Override
    public ProcessDefinition getProcessDefinitionByKey(String definitionKey) {
        return repositoryService.createProcessDefinitionQuery().
                processDefinitionKey(definitionKey).latestVersion().singleResult();
    }

    /**
     * Get process definition by deployment id
     * @param deploymentId Deployment id
     * @return Process definition
     */
    @Override
    public ProcessDefinition getProcessDefinitionByDeploymentId(String deploymentId) {
        return repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
    }

    /**
     * Get process definition by process instance id
     * @param processInstanceId Process instance id
     * @return Process definition
     */
    @Override
    public ProcessDefinition getProcessDefinitionByProcessInstanceId(String processInstanceId) {
        ProcessInstance processInstance = flowableProcessInstanceService.getProcessInstanceById(processInstanceId);
        return processInstance != null ? getProcessDefinitionById(processInstance.getProcessDefinitionId()) : null;
    }

    /**
     * Get all process definitions
     * @return List of process definitions
     */
    @Override
    public List<ProcessDefinition> getAllProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().desc().list();
    }

    /**
     * Get all last process definitions
     * @return List of process definitions
     */
    @Override
    public List<ProcessDefinition> getAllLastProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery().latestVersion().list();
    }

    /**
     * Get all process definitions by key
     * @param definitionKey Process definition key
     * @return List of process definitions
     */
    @Override
    public List<ProcessDefinition> getAllProcessDefinitionsByKey(String definitionKey) {
        return repositoryService.createProcessDefinitionQuery().
                processDefinitionKey(definitionKey).
                orderByProcessDefinitionVersion().desc().
                list();
    }
}
