package ru.citeck.ecos.records.rest;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.servlet.FormData;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;

import java.io.IOException;

/**
 * @author Pavel Simonov
 */
@Slf4j
public class RecordsMutatePost extends AbstractWebScript {

    private static final String FIELD_ID = "id";

    private static final String FILE_FIELD_MIMETYPE = "mimetype";
    private static final String FILE_FIELD_FILENAME = "filename";
    private static final String FILE_FIELD_CONTENT = "content";
    private static final String FILE_FIELD_CONTENT_TYPE = "type";

    private static final String CONTENT_TYPE_TEXT = "text";

    private RecordsService recordsService;
    private RecordsRestUtils utils;
    private TransactionService transactionService;

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
            ObjectData attributes = new ObjectData();

            FormData data = (FormData) req.parseContent();

            for (FormData.FormField field : data.getFields()) {

                String value = field.getValue();

                if (StringUtils.isBlank(value)) {
                    continue;
                }

                String fieldName = field.getName();

                if (FIELD_ID.equals(fieldName)) {
                    recordMeta.setId(RecordRef.valueOf(field.getValue()));
                } else {

                    if (field.getIsFile()) {
                        ObjectData fileData = new ObjectData();
                        fileData.set(FILE_FIELD_MIMETYPE, field.getMimetype());
                        fileData.set(FILE_FIELD_FILENAME, field.getFilename());
                        fileData.set(FILE_FIELD_CONTENT, field.getContent().getContent());
                        fileData.set(FILE_FIELD_CONTENT_TYPE, CONTENT_TYPE_TEXT);
                        attributes.set(fieldName, fileData);
                    } else {
                        attributes.set(fieldName, field.getValue());
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

        RecordsMutResult result = mutate(request);

        utils.writeRespRecords(res, result, RecordsMutResult::getRecords, request.isSingleRecord);
    }

    private RecordsMutResult mutate(Request request) {
        RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        return helper.doInTransaction(() -> {
            try {
                return recordsService.mutate(request);
            } catch (Exception e) {
                log.debug("Exception while mutation", e);
                throw e;
            }
        }, false, true);
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @Autowired
    public void setUtils(RecordsRestUtils utils) {
        this.utils = utils;
    }

    @Autowired
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public static class Request extends RecordsMutation {

        boolean isSingleRecord = false;

        void setRecord(RecordMeta record) {
            isSingleRecord = true;
            getRecords().add(record);
        }
    }
}
