package ru.citeck.ecos.webscripts.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.utils.NewUIUtils;

import java.io.IOException;
import java.io.Writer;

public class CustomUrlForRedirectToUIGet extends AbstractWebScript {

    private static final String SHARE_PAGE_URL = "/share/page";

    private static final String RESPONSE_TEMPLATE = "{\"url\":\"%s\"}";

    private NewUIUtils newUIUtils;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String url;
        if (newUIUtils.isNewUIEnabled()) {
            url = newUIUtils.getNewUIRedirectUrl();
        } else {
            url = SHARE_PAGE_URL;
        }

        try (Writer out = res.getWriter()) {
            out.write(String.format(RESPONSE_TEMPLATE, url));
            res.setStatus(Status.STATUS_OK);
        }
    }

    @Autowired
    public void setNewUIUtils(NewUIUtils newUIUtils) {
        this.newUIUtils = newUIUtils;
    }
}
