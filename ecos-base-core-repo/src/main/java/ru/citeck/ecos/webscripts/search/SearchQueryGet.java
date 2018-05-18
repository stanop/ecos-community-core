package ru.citeck.ecos.webscripts.search;

import lombok.Setter;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.search.SearchQuery;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class SearchQueryGet extends DeclarativeWebScript {

    private static final String PARAM_QUERY = "query";
    private static final String PARAM_SCHEMA = "schema";
    private static final String PARAM_MAX_ITEMS = "maxItems";
    private static final String PARAM_SKIP_COUNT = "skipCount";
    private static final String PARAM_LANGUAGE = "language";
    private static final String PARAM_CACHE_AGE = "cacheAge";

    @Setter
    private SearchQuery searchObject;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        String searchQuery = req.getParameter(PARAM_QUERY);
        if (searchQuery == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Parameter '" + PARAM_QUERY + "' not specified");
            return null;
        }

        cache.setMaxAge((long) getInt(req, PARAM_CACHE_AGE, 0));

        int maxItems = getInt(req, PARAM_MAX_ITEMS, SearchQuery.DEFAULT_MAX_ITEMS);
        int skipCount = getInt(req, PARAM_SKIP_COUNT, SearchQuery.DEFAULT_SKIP_COUNT);
        String language = getString(req, PARAM_LANGUAGE, SearchQuery.DEFAULT_LANGUAGE);
        JSONObject schema = getSchema(req.getParameter(PARAM_SCHEMA));

        String resultStr;
        try {
            resultStr = searchObject.queryNodes(searchQuery, maxItems, skipCount, language, schema).toString();
        } catch (JSONException e) {
            throw new AlfrescoRuntimeException("JSONException", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", resultStr);

        return result;
    }

    private JSONObject getSchema(String schema) {
        try {
            return StringUtils.isNotBlank(schema) ? new JSONObject(schema) : SearchQuery.DEFAULT_SCHEMA;
        } catch (JSONException e) {
            throw new AlfrescoRuntimeException("Schema parse failed", e);
        }
    }

    private String getString(WebScriptRequest req, String name, String orValue) {
        String value = req.getParameter(name);
        return StringUtils.isNotBlank(value) ? value : orValue;
    }

    private int getInt(WebScriptRequest req, String name, int orValue) {
        String value = req.getParameter(name);
        return StringUtils.isNotBlank(value) ? Integer.parseInt(value) : orValue;
    }

}
