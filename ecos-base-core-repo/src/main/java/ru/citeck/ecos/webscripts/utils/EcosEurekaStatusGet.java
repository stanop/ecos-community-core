package ru.citeck.ecos.webscripts.utils;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Writer;

public class EcosEurekaStatusGet extends AbstractWebScript {

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try (Writer out = res.getWriter()) {
            out.write("{\"status\":\"UP\"}");
            res.setStatus(Status.STATUS_OK);
        }
    }
}
