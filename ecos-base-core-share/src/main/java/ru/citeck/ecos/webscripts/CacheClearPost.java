package ru.citeck.ecos.webscripts;

import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.json.JSONUtils;
import ru.citeck.ecos.share.template.CiteckUtilsTemplate;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class CacheClearPost extends AbstractWebScript {

    private static final String PARAM_UPDATE_BUST = "updateCacheBust";
    private static final String PARAM_CLEAR_CACHE = "clearCache";

    private CiteckUtilsTemplate templateUtils;

    private JSONUtils jsonUtils = new JSONUtils();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, Boolean> result = new HashMap<>();

        String updateBust = req.getParameter(PARAM_UPDATE_BUST);
        if (Boolean.TRUE.toString().equals(updateBust)) {
            templateUtils.updateCacheBust();
            result.put("bustUpdated", true);
        }
        String clearCache = req.getParameter(PARAM_CLEAR_CACHE);
        if (Boolean.TRUE.toString().equals(clearCache)) {
            templateUtils.clearCache();
            result.put("cacheCleared", true);
        }

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        try (Writer writer = res.getWriter()) {
            writer.write(jsonUtils.toJSONString(result));
        }
        res.setStatus(Status.STATUS_OK);
    }

    public void setTemplateUtils(CiteckUtilsTemplate templateUtils) {
        this.templateUtils = templateUtils;
    }
}
