package ru.citeck.ecos.records.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.rest.QueryBody;
import ru.citeck.ecos.records2.request.rest.RestHandler;
import ru.citeck.ecos.records2.utils.MandatoryParam;

import java.io.IOException;

public class RecordsQueryGet extends AbstractWebScript {

    public static final String PARAM_ATTRIBUTE = "att";
    public static final String PARAM_RECORD = "rec";

    private RestHandler restHandler;
    private RecordsRestUtils recordsRestUtils;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String attribute = req.getParameter(PARAM_ATTRIBUTE);
        String record = req.getParameter(PARAM_RECORD);

        MandatoryParam.checkString(PARAM_ATTRIBUTE, attribute);
        MandatoryParam.checkString(PARAM_RECORD, record);

        QueryBody body = new QueryBody();
        body.setAttribute(attribute);
        body.setRecord(RecordRef.valueOf(record));

        recordsRestUtils.writeResp(res, restHandler.queryRecords(body));
    }

    @Autowired
    public void setRestHandler(RestHandler restHandler) {
        this.restHandler = restHandler;
    }

    @Autowired
    public void setRecordsRestUtils(RecordsRestUtils recordsRestUtils) {
        this.recordsRestUtils = recordsRestUtils;
    }
}
