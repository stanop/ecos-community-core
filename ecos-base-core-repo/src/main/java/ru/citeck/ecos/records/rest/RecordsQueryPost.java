package ru.citeck.ecos.records.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.records2.request.rest.QueryBody;
import ru.citeck.ecos.records2.request.rest.RestQueryHandler;

import java.io.IOException;

/**
 * @author Pavel Simonov
 */
public class RecordsQueryPost extends AbstractWebScript {

    private RecordsRestUtils utils;
    private RestQueryHandler restQueryHandler;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        QueryBody request = utils.readBody(req, QueryBody.class);
        utils.writeResp(res, restQueryHandler.queryRecords(request));
    }

    @Autowired
    public void setRestQueryHandler(RestQueryHandler restQueryHandler) {
        this.restQueryHandler = restQueryHandler;
    }

    @Autowired
    public void setUtils(RecordsRestUtils utils) {
        this.utils = utils;
    }
}
