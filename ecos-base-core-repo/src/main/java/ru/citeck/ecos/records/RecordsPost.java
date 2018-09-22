package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;

import java.io.IOException;

public class RecordsPost extends AbstractWebScript {

    private ObjectMapper objectMapper = new ObjectMapper();
    private RecordsService recordsService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Request request = objectMapper.readValue(req.getContent().getContent(), Request.class);
        RecordsResult records = recordsService.getRecords(request.sourceId, request.query);

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), records);
        res.setStatus(Status.STATUS_OK);
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    public static class Request {

        public RecordsQuery query;
        public String sourceId = "";

        @Override
        public String toString() {
            return "Request{" +
                    "query=" + query +
                    ", sourceId='" + sourceId + '\'' +
                    '}';
        }
    }
}
