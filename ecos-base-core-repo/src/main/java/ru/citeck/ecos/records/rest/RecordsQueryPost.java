package ru.citeck.ecos.records.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.records2.request.rest.QueryBody;
import ru.citeck.ecos.records2.request.rest.RestHandler;

import java.io.IOException;

/**
 * @author Pavel Simonov
 */
public class RecordsQueryPost extends AbstractWebScript {

    private RecordsRestUtils utils;
    private RestHandler restHandler;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        QueryBody request = utils.readBody(req, QueryBody.class);
        utils.writeResp(res, restHandler.queryRecords(request));
    }

    @Autowired
    public void setRestQueryHandler(RestHandler restHandler) {
        this.restHandler = restHandler;
    }

    @Autowired
    public void setUtils(RecordsRestUtils utils) {
        this.utils = utils;
    }
}
