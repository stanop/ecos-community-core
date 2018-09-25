package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.utils.json.mixin.NodeRefMixIn;
import ru.citeck.ecos.utils.json.mixin.QNameMixIn;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;

public class GroupActionPost extends AbstractWebScript {

    private ObjectMapper objectMapper = new ObjectMapper();
    private GroupActionService groupActionService;

    @PostConstruct
    public void init() {
        objectMapper.addMixInAnnotations(NodeRef.class, NodeRefMixIn.class);
        objectMapper.addMixInAnnotations(QName.class, QNameMixIn.class);
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        ActionData actionData = objectMapper.readValue(req.getContent().getContent(), ActionData.class);

        Response response = new Response();

        response.results = groupActionService.execute(actionData.nodes, actionData.config);

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
        public GroupActionConfig config;
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
                      include = JsonTypeInfo.As.WRAPPER_OBJECT)
        public List<?> nodes;
    }

    public static class Response<T> {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
                      include = JsonTypeInfo.As.WRAPPER_OBJECT)
        public ActionResults<T> results;
    }
}
