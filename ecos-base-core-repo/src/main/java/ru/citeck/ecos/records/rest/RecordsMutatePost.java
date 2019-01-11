package ru.citeck.ecos.records.rest;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;

import org.springframework.extensions.webscripts.servlet.FormData;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.request.mutation.RecordMut;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;

import java.io.IOException;

/**
 * @author Pavel Simonov
 */
public class RecordsMutatePost extends AbstractWebScript {

    private static final String FIELD_ID = "id";
    private static final String FIELD_PARENT = "parent";
    private static final String FIELD_PARENT_ATT = "parentAtt";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_ATT_PREFIX = "att_";

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

            RecordMut recordMut = new RecordMut();
            ObjectNode attributes = JsonNodeFactory.instance.objectNode();

            FormData data = (FormData) req.parseContent();

            for (FormData.FormField field : data.getFields()) {

                String value = field.getValue();

                if (StringUtils.isBlank(value)) {
                    continue;
                }

                String fieldName = field.getName();

                if (fieldName.startsWith(FIELD_ATT_PREFIX)) {

                    String attName = fieldName.substring(FIELD_ATT_PREFIX.length());

                    if (field.getIsFile()) {
                        ObjectNode fileData = attributes.with(attName);
                        fileData.put(FILE_FIELD_MIMETYPE, field.getMimetype());
                        fileData.put(FILE_FIELD_FILENAME, field.getFilename());
                        fileData.put(FILE_FIELD_CONTENT, field.getContent().getContent());
                        fileData.put(FILE_FIELD_CONTENT_TYPE, CONTENT_TYPE_TEXT);
                    } else {
                        attributes.put(attName, field.getValue());
                    }

                } else {

                    switch (fieldName) {

                        case FIELD_ID:

                            recordMut.setId(new RecordRef(value));
                            break;

                        case FIELD_PARENT:

                            recordMut.setParent(value);
                            break;

                        case FIELD_PARENT_ATT:

                            recordMut.setParentAtt(value);
                            break;

                        case FIELD_TYPE:

                            recordMut.setType(value);
                            break;
                    }
                }
            }

            recordMut.setAttributes(attributes);
            request = new Request();
            request.setRecord(recordMut);

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

        void setRecord(RecordMut record) {
            isSingleRecord = true;
            getRecords().add(record);
        }
    }
}
