package ru.citeck.ecos.webscripts.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.config.EcosConfigService;

import java.io.IOException;
import java.io.Writer;

public class IsNewUiRedirectEnabledGet extends AbstractWebScript {

    private static final String PARAM_NAME = "new-ui-redirect-enabled";

    private EcosConfigService ecosConfigService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        try (Writer out = res.getWriter()) {

            boolean value;
            try {
                Object objValue = ecosConfigService.getParamValue(PARAM_NAME);
                value = String.valueOf(objValue).equals(Boolean.TRUE.toString());
            } catch (Exception e) {
                value = false;
            }

            out.write("{\"enabled\":\"" + value + "\"}");
            res.setStatus(Status.STATUS_OK);
        }
    }

    @Autowired
    @Qualifier("ecosConfigService")
    public void setEcosConfigService(EcosConfigService ecosConfigService) {
        this.ecosConfigService = ecosConfigService;
    }
}
