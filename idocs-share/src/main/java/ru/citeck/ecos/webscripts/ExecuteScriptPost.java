package ru.citeck.ecos.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Undefined;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.processor.JSScriptProcessor;
import ru.citeck.ecos.utils.ValueConverter;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class ExecuteScriptPost  extends AbstractWebScript {

    private static final String MODEL_MESSAGES = "messages";

    private JSScriptProcessor scriptProcessor;
    private ScriptConfigModel scriptConfigModel;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");

        try {

            Request request = objectMapper.readValue(req.getContent().getContent(), Request.class);

            Map<String, Object> model = new HashMap<>();
            model.put(ProcessorModelHelper.MODEL_CONFIG, scriptConfigModel);

            List<String> messages = new ArrayList<>();
            model.put(MODEL_MESSAGES, messages);

            String script = String.format("function print(msg) {messages.add(msg);} %n%s", request.script);
            Object resultJs = scriptProcessor.executeScript(new Script(script), model);
            if (resultJs instanceof Undefined) {
                resultJs = "undefined";
            }
            Object resultJava = ValueConverter.convertValueForJava(resultJs);

            Map<String, Object> result = new HashMap<>();
            result.put("result", resultJava);
            if (messages.size() > 0) {
                result.put("messages", messages);
            }

            String resultStr = objectMapper.writeValueAsString(result);
            res.getWriter().write(resultStr);
            res.setStatus(Status.STATUS_OK);

        } catch (Exception e) {

            e.printStackTrace();
            res.getWriter().write(e.getMessage());
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
        }
    }

    public void setScriptProcessor(JSScriptProcessor scriptProcessor) {
        this.scriptProcessor = scriptProcessor;
    }

    public void setScriptConfigModel(ScriptConfigModel scriptConfigModel) {
        this.scriptConfigModel = scriptConfigModel;
    }

    private static class Request {
        public String script;
    }

    static class Script implements ScriptContent {

        private String script;

        public Script(String script) {
            this.script = script;
        }

        @Override
        public InputStream getInputStream() {
            return IOUtils.toInputStream(script);
        }

        @Override
        public Reader getReader() {
            return new StringReader(script);
        }

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public String getPathDescription() {
            return null;
        }

        @Override
        public boolean isCachable() {
            return false;
        }

        @Override
        public boolean isSecure() {
            return true;
        }
    }
}
