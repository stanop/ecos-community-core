package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.graphql.journal.JournalGqlPageInfoInput;
import ru.citeck.ecos.journals.records.JournalRecordsDAO;

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
    private JournalRecordsDAO journalRecordsDAO;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String journalId = req.getParameter(PARAM_JOURNAL_ID);

        RequestBody request = objectMapper.readValue(req.getContent().getContent(), RequestBody.class);
        ExecutionResult result = journalRecordsDAO.getRecordsWithData(
                request.query,
                request.language,
                journalId,
                request.pageInfo
        );

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), result.toSpecification());

        res.setStatus(Status.STATUS_OK);
    }

    private static class RequestBody {
        public String query;
        public String language;
        public JournalGqlPageInfoInput pageInfo;
    }
}
