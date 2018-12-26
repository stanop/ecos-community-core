package ru.citeck.ecos.records.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;

import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.request.mutation.RecordMut;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;

import java.io.IOException;

public class RecordsMutatePost extends AbstractWebScript {

    private RecordsService recordsService;
    private RecordsRestUtils utils;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Request request = utils.readBody(req, Request.class);
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
