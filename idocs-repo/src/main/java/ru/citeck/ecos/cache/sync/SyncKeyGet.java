package ru.citeck.ecos.cache.sync;

import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;

public class SyncKeyGet extends AbstractWebScript {

    private static final String PARAM_KEY = "key";

    private SyncKeysService syncKeysService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String key = req.getParameter(PARAM_KEY);
        ParameterCheck.mandatoryString(PARAM_KEY, key);

        res.setContentType(Format.TEXT.mimetype() + ";charset=UTF-8");
        res.getWriter().write(String.valueOf(syncKeysService.get(key)));
        res.setStatus(Status.STATUS_OK);
    }

    @Autowired
    public void setSyncKeysService(SyncKeysService syncKeysService) {
        this.syncKeysService = syncKeysService;
    }
}
