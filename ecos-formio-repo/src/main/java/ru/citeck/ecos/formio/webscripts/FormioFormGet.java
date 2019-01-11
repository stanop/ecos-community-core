package ru.citeck.ecos.formio.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.formio.FormioForm;
import ru.citeck.ecos.formio.FormioFormService;

import java.io.IOException;
import java.util.Optional;

public class FormioFormGet extends AbstractWebScript {

    /* params */
    private static final String PARAM_ID = "id";
    private static final String PARAM_KEY = "key";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_MODE = "mode";
    /* ------ */

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private FormioFormService formService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String type = req.getParameter(PARAM_TYPE);
        String key = req.getParameter(PARAM_KEY);
        String mode = req.getParameter(PARAM_MODE);
        String id = req.getParameter(PARAM_ID);

        Optional<FormioForm> form = formService.getForm(type, key, id, mode);

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");

        if (form.isPresent()) {
            objectMapper.writeValue(res.getOutputStream(), form.get());
            res.setStatus(Status.STATUS_OK);
        } else {
            res.setStatus(Status.STATUS_NOT_FOUND);
        }
    }
}
