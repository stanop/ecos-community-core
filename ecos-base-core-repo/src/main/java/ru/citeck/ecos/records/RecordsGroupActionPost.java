package ru.citeck.ecos.records;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class RecordsGroupActionPost extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(RecordsGroupActionPost.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    private RecordsService recordsService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        ActionData actionData = objectMapper.readValue(req.getContent().getContent(), ActionData.class);

        if (logger.isDebugEnabled()) {
            logger.debug("Request: " + actionData);
        }

        Response response = new Response();

        try (Writer writer = res.getWriter()) {
            try {
                response.results = recordsService.executeAction(actionData.nodes,
                        actionData.actionId,
                        actionData.config);

                res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
                objectMapper.writeValue(writer, response);
                res.setStatus(Status.STATUS_OK);
            } catch (Exception e) {
                Throwable retryCause = RetryingTransactionHelper.extractRetryCause(e);
                if (retryCause != null) {
                    throw e;
                }
                logger.error(e);
                writer.write(e.getMessage());
                res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ActionData {
        public String actionId;
        public GroupActionConfig config;
        public List<RecordRef> nodes;

        @Override
        public String toString() {
            return "ActionData{" +
                    "actionId='" + actionId + '\'' +
                    ", config=" + config +
                    ", nodes=" + nodes +
                    '}';
        }
    }

    public static class Response {
        public ActionResults<RecordRef> results;
    }
}
