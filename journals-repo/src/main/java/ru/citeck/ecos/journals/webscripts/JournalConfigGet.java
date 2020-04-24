package ru.citeck.ecos.journals.webscripts;

import lombok.Data;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.domain.JournalMeta;
import ru.citeck.ecos.journals.domain.JournalTypeColumn;
import ru.citeck.ecos.journals.service.JournalColumnService;
import ru.citeck.ecos.journals.service.JournalMetaService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JournalConfigGet extends AbstractWebScript {

    private static final String PARAM_JOURNAL = "journalId";

    private JournalService journalService;
    private JournalColumnService journalColumnService;
    private JournalMetaService journalMetaService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");

        String journalId = req.getParameter(PARAM_JOURNAL);
        Response response = executeImpl(journalId);

        Json.getMapper().write(res.getWriter(), response);
        res.setStatus(Status.STATUS_OK);
    }

    private Response executeImpl(String journalId) {

        if (StringUtils.isBlank(journalId)) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "journalId is a mandatory parameter!");
        }

        JournalType journalType = journalService.getJournalType(journalId);

        if (journalType == null) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Journal with id '" + journalId + "' is not found!");
        }

        String sourceId = journalType.getDataSource();
        if (sourceId == null) {
            sourceId = "";
        }

        JournalMeta journalMeta = journalMetaService.getJournalMeta(journalId);

        List<JournalTypeColumn> columns =
            journalColumnService.getJournalTypeColumns(journalType, journalMeta.getMetaRecord());

        Response response = new Response();
        response.setId(journalType.getId());
        response.setColumns(columns);
        response.setMeta(journalMeta);
        response.setSourceId(sourceId);
        response.setParams(journalType.getOptions());

        return response;
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }

    @Autowired
    public void setJournalColumnService(JournalColumnService journalColumnService) {
        this.journalColumnService = journalColumnService;
    }

    @Autowired
    public void setJournalMetaService(JournalMetaService journalMetaService) {
        this.journalMetaService = journalMetaService;
    }

    @Data
    static class Response {
        String id;
        String sourceId;
        JournalMeta meta;
        List<JournalTypeColumn> columns;
        Map<String, String> params;
    }
}
