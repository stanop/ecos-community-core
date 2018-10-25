package ru.citeck.ecos.webscripts.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.utils.WorkflowUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentTasksManualGet extends AbstractWebScript {

    private static final String PARAM_NODEREF = "nodeRef";

    private WorkflowUtils workflowUtils;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String nodeRefStr = req.getParameter(PARAM_NODEREF);
        if (StringUtils.isBlank(nodeRefStr) || !NodeRef.isNodeRef(nodeRefStr)) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "nodeRef is a mandatory parameter");
        }

        NodeRef documentRef = new NodeRef(nodeRefStr);

        List<WorkflowTask> tasks = workflowUtils.getDocumentUserTasks(documentRef, true);

        Response response = new Response();
        response.tasks = tasks.stream().map(this::formatTask).collect(Collectors.toList());

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), response);
        res.setStatus(Status.STATUS_OK);
    }

    private Task formatTask(WorkflowTask task) {
        Task result = new Task();
        result.id = task.getId();
        result.title = workflowUtils.getTaskTitle(task);
        result.description = task.getDescription();
        return result;
    }

    @Autowired
    public void setWorkflowUtils(WorkflowUtils workflowUtils) {
        this.workflowUtils = workflowUtils;
    }

    private static class Response {
        public List<Task> tasks;
    }

    private static class Task {
        public String id;
        public String title;
        public String description;
    }
}
