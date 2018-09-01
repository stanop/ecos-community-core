package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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

        response.results = groupActionService.execute(actionData.nodes,
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ActionData {
        public String actionId;
        public GroupActionConfig config;
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
                      include = JsonTypeInfo.As.WRAPPER_OBJECT)
        public List<?> nodes;
    }

    public static class Response<T> {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
                      include = JsonTypeInfo.As.WRAPPER_OBJECT)
        public List<ActionResult<T>> results;
    }
}
