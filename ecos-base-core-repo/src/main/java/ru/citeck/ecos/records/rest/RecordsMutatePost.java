package ru.citeck.ecos.records.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;

import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.request.mutation.RecordMut;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;

import java.io.IOException;

public class RecordsMutatePost extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(RecordsMutatePost.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    private RecordsService recordsService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Request request = objectMapper.readValue(req.getContent().getContent(), Request.class);

        RecordsMutResult result = recordsService.mutate(request);

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), result);
        res.setStatus(Status.STATUS_OK);
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    public static class Request extends RecordsMutation {

        void setRecord(RecordMut record) {
            getRecords().add(record);
        }
    }
}
