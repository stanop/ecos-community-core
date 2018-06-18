package ru.citeck.ecos.flowable.services.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import ru.citeck.ecos.flowable.services.FlowableCustomCommentService;
import ru.citeck.ecos.flowable.services.FlowableProcessDefinitionService;
import ru.citeck.ecos.flowable.services.FlowableTaskService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Flowable custom comment service
 */
public class FlowableCustomCommentServiceImpl implements FlowableCustomCommentService {

    private static final QName COMMENT_FIELD_ID = QName.createQName("http://www.citeck.ru/model/workflow/1.1", "customCommentFieldId");

    /**
     * Flowable task service
     */
    private FlowableTaskService flowableTaskService;

    /**
     * Flowable process definition service
     */
    private FlowableProcessDefinitionService flowableProcessDefinitionService;

    /**
     * Search service
     */
    private SearchService searchService;

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Get comments fields by task id
     * @param taskId Task id
     * @return List of comment fields
     */
    @Override
    public List<String> getFieldIdsByTaskId(String taskId) {
        Task task = flowableTaskService.getTaskById(taskId);
        if (task == null) {
            return Collections.emptyList();
        }
        ProcessDefinition processDefinition = flowableProcessDefinitionService.getProcessDefinitionById(task.getProcessDefinitionId());
        if (processDefinition == null) {
            return Collections.emptyList();
        }
        return getProcessCustomComments(processDefinition.getKey());
    }

    /**
     * Get comments fields by process definitions id
     * @param processDefinitionId Process definition id
     * @return List of comment fields
     */
    @Override
    public List<String> getFieldIdsByProcessDefinitionId(String processDefinitionId) {
        ProcessDefinition processDefinition = flowableProcessDefinitionService.getProcessDefinitionById(processDefinitionId);
        if (processDefinition == null) {
            return Collections.emptyList();
        }
        return getProcessCustomComments(processDefinition.getKey());
    }

    /**
     * Get properties custom comments
     * @param processDefinitionKey Process definition key
     * @return List of field ids
     */
    private List<String> getProcessCustomComments(String processDefinitionKey) {
        List<NodeRef> links = getProcessCustomCommentsLinks(processDefinitionKey);
        List<String> result = new ArrayList<>(links.size());
        for (NodeRef link : links) {
            result.add((String) nodeService.getProperty(link, COMMENT_FIELD_ID));
        }
        return result;
    }

    /**
     * Get documents by offset
     * @return Set of documents
     */
    private List<NodeRef> getProcessCustomCommentsLinks(String processDefinitionKey) {
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        parameters.setQuery("TYPE:\"cwf:flowableProcessCustomComment\" " +
                "AND @cwf\\:processDefinitionKey:\""  + processDefinitionKey + "\"");
        parameters.addSort("@cm:created", true);
        ResultSet resultSet = searchService.query(parameters);
        return resultSet.getNodeRefs();
    }

    /** Setters */

    public void setFlowableTaskService(FlowableTaskService flowableTaskService) {
        this.flowableTaskService = flowableTaskService;
    }

    public void setFlowableProcessDefinitionService(FlowableProcessDefinitionService flowableProcessDefinitionService) {
        this.flowableProcessDefinitionService = flowableProcessDefinitionService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
