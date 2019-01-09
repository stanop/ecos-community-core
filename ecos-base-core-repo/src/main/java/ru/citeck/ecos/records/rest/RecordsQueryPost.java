package ru.citeck.ecos.records.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.request.RespRecord;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;

import java.io.IOException;
import java.util.*;

public class RecordsQueryPost extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(RecordsQueryPost.class);

    private RecordsService recordsService;
    private RecordsRestUtils utils;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Request request = utils.readBody(req, Request.class);

        if (request.query != null && request.records != null) {
            logger.warn("There must be one of 'records' or 'query' field " +
                        "but found both. 'records' field will be ignored");
        }
        if (request.attributes != null && request.schema != null) {
            logger.warn("There must be one of 'attributes' or 'schema' field " +
                        "but found both. 'schema' field will be ignored");
        }

        boolean isFlatResult = Boolean.TRUE.equals(request.flat);

        RecordsResult<?> recordsResult;

        if (request.query != null) {

            if (request.attributes != null) {

                recordsResult = recordsService.getRecords(request.query, getAttributes(request));

            } else if (request.schema != null) {

                recordsResult = recordsService.getRecords(request.query, request.schema, isFlatResult);

            } else {

                recordsResult = recordsService.getRecords(request.query);
            }
        } else {

            if (request.records == null) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                                             "At least 'records' or 'query' must be specified");
            }
            if (request.schema == null && request.attributes == null) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                                             "You must specify 'schema' or 'attributes' for records");
            }

            if (request.attributes == null) {

                RecordsResult<ObjectNode> metaResult = new RecordsResult<>();
                metaResult.setTotalCount(request.records.size());
                metaResult.setHasMore(false);
                metaResult.setRecords(recordsService.getMeta(request.records, request.schema, isFlatResult));
                recordsResult = metaResult;

            } else {

                RecordsResult<RespRecord> metaResult = new RecordsResult<>();
                metaResult.setRecords(recordsService.getMeta(request.records, getAttributes(request)));
                recordsResult = metaResult;
            }
        }

        utils.writeRespRecords(res, recordsResult, RecordsResult::getRecords, request.isSingleRecord);
    }

    private Map<String, String> getAttributes(Request request) {

        Map<String, String> result = new HashMap<>();

        if (request.attributes.isArray()) {
            for (int i = 0; i < request.attributes.size(); i++) {
                String field = request.attributes.get(i).asText();
                result.put(field, field);
            }
        } else {
            Iterator<String> names = request.attributes.fieldNames();
            while (names.hasNext()) {
                String fieldKey = names.next();
                result.put(fieldKey, request.attributes.get(fieldKey).asText());
            }
        }

        return result;
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @Autowired
    public void setUtils(RecordsRestUtils utils) {
        this.utils = utils;
    }

    public static class Request {

        public List<RecordRef> records;
        public RecordsQuery query;
        public String schema;
        public Boolean flat;
        public JsonNode attributes;

        boolean isSingleRecord = false;

        public void setRecord(RecordRef record) {
            if (records == null) {
                records = new ArrayList<>();
            }
            isSingleRecord = true;
            records.add(record);
        }

        @Override
        public String toString() {
            return "Request{" + query + '}';
        }
    }
}
