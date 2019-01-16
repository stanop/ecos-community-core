package ru.citeck.ecos.records.rest;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;

import org.springframework.extensions.webscripts.servlet.FormData;
import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;

import java.io.IOException;

/**
 * @author Pavel Simonov
 */
public class RecordsMutatePost extends AbstractWebScript {

    private static final String FIELD_ID = "id";
    private static final String FIELD_PARENT = "_parent";
    private static final String FIELD_PARENT_ATT = "_parentAtt";
    private static final String FIELD_TYPE = "_type";

    private static final String FILE_FIELD_MIMETYPE = "mimetype";
    private static final String FILE_FIELD_FILENAME = "filename";
    private static final String FILE_FIELD_CONTENT = "content";
    private static final String FILE_FIELD_CONTENT_TYPE = "type";

    private static final String CONTENT_TYPE_TEXT = "text";

    private RecordsService recordsService;
    private RecordsRestUtils utils;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Request request;

        String contentType = req.getContentType();
        if (contentType == null) {
            contentType = "";
        }

        if (contentType.contains(MimetypeMap.MIMETYPE_JSON)) {

            request = utils.readBody(req, Request.class);

        } else if (contentType.contains(WebScriptRequestImpl.MULTIPART_FORM_DATA)) {

            RecordMeta recordMeta = new RecordMeta();
            ObjectNode attributes = JsonNodeFactory.instance.objectNode();

            FormData data = (FormData) req.parseContent();

            for (FormData.FormField field : data.getFields()) {

                String value = field.getValue();

                if (StringUtils.isBlank(value)) {
                    continue;
                }

                String fieldName = field.getName();

                if (FIELD_ID.equals(fieldName)) {
                    recordMeta.setId(new RecordRef(field.getValue()));
                } else {

                    if (field.getIsFile()) {
                        ObjectNode fileData = attributes.with(fieldName);
                        fileData.put(FILE_FIELD_MIMETYPE, field.getMimetype());
                        fileData.put(FILE_FIELD_FILENAME, field.getFilename());
                        fileData.put(FILE_FIELD_CONTENT, field.getContent().getContent());
                        fileData.put(FILE_FIELD_CONTENT_TYPE, CONTENT_TYPE_TEXT);
                    } else {
                        attributes.put(fieldName, field.getValue());
                    }
                }
            }

            recordMeta.setAttributes(attributes);
            request = new Request();
            request.setRecord(recordMeta);

        } else {

            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                                         "Content type " + req.getContentType() + " is not supported");
        }

        RecordsMutResult result = recordsService.mutate(request);
        utils.writeRespRecords(res, result, RecordsMutResult::getRecords, request.isSingleRecord);
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @Autowired
    public void setUtils(RecordsRestUtils utils) {
        this.utils = utils;
    }

    public static class Request extends RecordsMutation {

        boolean isSingleRecord = false;

        void setRecord(RecordMeta record) {
            isSingleRecord = true;
            getRecords().add(record);
        }
    }
}
