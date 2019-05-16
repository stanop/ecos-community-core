package ru.citeck.ecos.eform.webscripts;

import lombok.extern.log4j.Log4j;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.Map;

@Log4j
public class FileUploadEformPost extends DeclarativeWebScript {
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {


        log.error("Test message calling " + this.getClass());


        Map<String, Object> result = new HashMap<>();
        result.put("result", "test");
        return result;
    }
}
