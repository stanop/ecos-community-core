package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;

import java.io.IOException;
import java.util.List;

public class RecordsPost extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(RecordsPost.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    private RecordsService recordsService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Request request = objectMapper.readValue(req.getContent().getContent(), Request.class);
        if (request.query != null && request.records != null) {
            logger.warn("There must be one of 'records' or 'query' field " +
                        "but found both. 'records' field will be ignored");
        }

        RecordsResult<?> recordsResult;

        if (request.query != null) {
            if (request.schema != null) {
                recordsResult = recordsService.getRecords(request.query, request.schema);
            } else {
                recordsResult = recordsService.getRecords(request.query);
            }
        } else {

            if (request.records == null) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                                             "At least 'records' or 'query' must be specified");
            }
            if (request.schema == null) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                                             "When records specified parameter 'schema' is mandatory");
            }

            RecordsResult<ObjectNode> metaResult = new RecordsResult<>();
            metaResult.setTotalCount(request.records.size());
            metaResult.setHasMore(false);
            metaResult.setRecords(recordsService.getMeta(request.records, request.schema));
            recordsResult = metaResult;
        }

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), recordsResult);
        res.setStatus(Status.STATUS_OK);
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    public static class Request {

        public List<RecordRef> records;
        public RecordsQuery query;
        public String schema;

        @Override
        public String toString() {
            return "Request{" + query + '}';
        }
    }
}
