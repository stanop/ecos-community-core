package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.journals.JournalService;

import java.io.IOException;

/**
 * @author Pavel Simonov
 */
public class JournalGqlSchemaGet extends AbstractWebScript {

    private static final String PARAM_JOURNAL = "journalId";

    private ObjectMapper objectMapper = new ObjectMapper();

    private JournalService journalService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String journalId = req.getParameter(PARAM_JOURNAL);

        if (StringUtils.isBlank(journalId)) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "journalId is a mandatory parameter!");
        }

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");

        Response response = new Response();
        response.setSchema(journalService.getJournalGqlSchema(journalId));
        response.setJournalId(journalId);

        objectMapper.writeValue(res.getOutputStream(), response);

        res.setStatus(Status.STATUS_OK);
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }

    static class Response {
        @Getter @Setter String journalId;
        @Getter @Setter String schema;
    }
}
