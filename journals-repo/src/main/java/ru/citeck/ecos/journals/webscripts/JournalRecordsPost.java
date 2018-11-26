package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.journals.JournalService;

import java.io.IOException;

/**
 * Get journal records based on query in request body and journalId
 * Webscript is replacement for criteria-search.post which return too much information
 *
 * @author Pavel Simonov
 */
public class JournalRecordsPost extends AbstractWebScript {

    //========PARAMS========
    private static final String PARAM_JOURNAL_ID = "journalId";
    //=======/PARAMS========

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JournalService journalService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String journalId = req.getParameter(PARAM_JOURNAL_ID);

        RequestBody request = objectMapper.readValue(req.getContent().getContent(), RequestBody.class);

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), journalService.getRecordsWithData(
                journalId,
                request.query,
                request.language,
                request.pageInfo,
                request.debug
        ));

        res.setStatus(Status.STATUS_OK);
    }

    private static class RequestBody {
        public String query;
        public String language;
        public boolean debug;
        public JGqlPageInfoInput pageInfo;
    }
}
