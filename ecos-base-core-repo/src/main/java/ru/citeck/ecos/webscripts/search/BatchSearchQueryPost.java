package ru.citeck.ecos.webscripts.search;

import org.alfresco.error.AlfrescoRuntimeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.search.SearchQuery;

import java.util.*;

public class BatchSearchQueryPost extends DeclarativeWebScript {

    private static final String PARAM_QUERY = "query";
    private static final String PARAM_PARAMS = "params";

    private static final String PLACEHOLDER = "%?%";

    private SearchQuery searchObject;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        JSONObject searchQuery = (JSONObject) req.parseContent();

        if (searchQuery == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Parameter '" + PARAM_QUERY + "' not specified");
            return null;
        }

        String result;
        try {
            JSONArray results = new JSONArray();

            String query = searchQuery.getString(PARAM_QUERY);
            JSONArray paramSets = searchQuery.getJSONArray(PARAM_PARAMS);

            List<List<String>> params = toArray(paramSets);

            for (List<String> paramSet : params) {
                String preparedQuery = String.format(
                        query.replace(PLACEHOLDER, "%s"),
                        paramSet.toArray());

                JSONObject setResult = searchObject.queryNodes(preparedQuery);

                Map<String, Object> map = new HashMap<>();
                map.put("params", new JSONArray(paramSet));
                map.put("result", setResult);
                results.put(new JSONObject(map));
            }

            result = results.toString();

        } catch (JSONException e) {
            throw new AlfrescoRuntimeException("Error on parsing json query parameter.", e);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("result", result);
        return map;
    }

    private List<List<String>> toArray(JSONArray jsonArray) {
        List<List<String>> arr = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            List<String> set = new ArrayList<>();

            JSONArray paramSet = jsonArray.optJSONArray(i);

            if (paramSet != null) {
                for (int j = 0; j < paramSet.length(); j++) {
                    set.add(paramSet.optString(j));
                }
            }
            else {
                set.add(jsonArray.optString(i));
            }

            arr.add(set);
        }

        return arr;
    }

    public void setSearchObject(SearchQuery searchObject) {
        this.searchObject = searchObject;
    }
}
