package ru.citeck.ecos.records.rest;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;

import java.io.IOException;

/**
 * @author Pavel Simonov
 */
public class RecordsDeletePost extends AbstractWebScript {

    private RecordsService recordsService;
    private RecordsRestUtils utils;
    private TransactionService transactionService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Request request = utils.readBody(req, Request.class);

        RecordsDelResult delete = delete(request);

        utils.writeResp(res, delete);
    }

    private RecordsDelResult delete(Request request) {
        RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        return helper.doInTransaction(() ->
                recordsService.delete(request),
            false, true);
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

    public static class Request extends RecordsDeletion {

        void setRecord(RecordRef record) {
            getRecords().add(record);
        }
    }
}
