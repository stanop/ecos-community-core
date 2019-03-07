package ru.citeck.ecos.records.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;

import java.io.IOException;

/**
 * @author Pavel Simonov
 */
public class RecordsDeletePost extends AbstractWebScript {

    private RecordsService recordsService;
    private RecordsRestUtils utils;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Request request = utils.readBody(req, Request.class);
        utils.writeResp(res, recordsService.delete(request));
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @Autowired
    public void setUtils(RecordsRestUtils utils) {
        this.utils = utils;
    }

    public static class Request extends RecordsDeletion {

        void setRecord(RecordRef record) {
            getRecords().add(record);
        }
    }
}
