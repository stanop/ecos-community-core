package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.List;

public class GroupActionPost extends AbstractWebScript {

    private ObjectMapper objectMapper = new ObjectMapper();
    private GroupActionService groupActionService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        ActionData actionData = objectMapper.readValue(req.getContent().getContent(), ActionData.class);

        Response response = new Response();
        response.results = groupActionService.execute(actionData.records,
                                                      actionData.actionId,
                                                      actionData.config);

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), response);
        res.setStatus(Status.STATUS_OK);
    }

    @Autowired
    public void setGroupActionService(GroupActionService groupActionService) {
        this.groupActionService = groupActionService;
    }

    public static class ActionData {
        public String actionId;
        public GroupActionConfig config;
        public List<String> records;
    }

    public static class Response {
        public List<ActionResult<String>> results;
    }
}
