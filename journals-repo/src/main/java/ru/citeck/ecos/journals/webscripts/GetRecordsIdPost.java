package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.datasource.JournalDataSource;
import ru.citeck.ecos.journals.records.JournalRecordsResult;

import java.io.IOException;

public class GetRecordsIdPost extends AbstractWebScript {

    private ObjectMapper objectMapper = new ObjectMapper();
    private ServiceRegistry serviceRegistry;

    public GetRecordsIdPost() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        Request request = parseRequest(webScriptRequest);
        JournalDataSource dataSource = findJournalDataSource(request.datasource);
        GqlContext gqlContext = new GqlContext(serviceRegistry);
        JournalRecordsResult ids = null;
        try {
            ids = dataSource.queryIds(gqlContext, request.query, request.language, request.jGqlPageInfoInput);
        } catch (Exception e) {
            throw new IOException(e);
        }
        objectMapper.writeValue(webScriptResponse.getOutputStream(), ids);
        webScriptResponse.setStatus(Status.STATUS_OK);
    }

    private Request parseRequest(WebScriptRequest req) {
        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }

        if (MimetypeMap.MIMETYPE_JSON.equals(contentType)) {
            try {
                String content = req.getContent().getContent();
                return objectMapper.readValue(content, GetRecordsIdPost.Request.class);
            } catch (IOException e) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + e.getMessage(), e);
            }
        } else {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Content type must be JSON");
        }
    }

    private JournalDataSource findJournalDataSource(String datasource) {
        QName datasourceQname = QName.createQName(null, datasource);
        JournalDataSource dataSource;
        try {
            dataSource = (JournalDataSource) serviceRegistry.getService(datasourceQname);
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Error while finding datasource: " + datasource + ". Error: " + e.getMessage(), e);
        }
        return dataSource;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class Request {
        public String query;
        public String language;
        public JGqlPageInfoInput jGqlPageInfoInput;
        public String datasource;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
