package ru.citeck.ecos.webscripts.utils;

import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.io.Writer;

public class EcosTouchGet extends AbstractWebScript {

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try (Writer out = res.getWriter()) {
            out.write("OK");
            res.setStatus(Status.STATUS_OK);
        }
    }
}
