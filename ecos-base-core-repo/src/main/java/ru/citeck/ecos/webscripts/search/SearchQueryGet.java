package ru.citeck.ecos.webscripts.search;

import lombok.Setter;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.attr.NodeAttributeService;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class SearchQueryGet extends DeclarativeWebScript {

    private static final int DEFAULT_MAX_ITEMS = 1000;
    private static final int DEFAULT_SKIP_COUNT = 0;
    private static final String DEFAULT_LANGUAGE = SearchService.LANGUAGE_FTS_ALFRESCO;

    private static final String PARAM_QUERY = "query";
    private static final String PARAM_SCHEMA = "schema";
    private static final String PARAM_MAX_ITEMS = "maxItems";
    private static final String PARAM_SKIP_COUNT = "skipCount";
    private static final String PARAM_LANGUAGE = "language";
    private static final String PARAM_CACHE_AGE = "cacheAge";

    private static final JSONObject DEFAULT_SCHEMA = new JSONObject();

    static {
        try {
            DEFAULT_SCHEMA.put("nodeRef", "");
            JSONObject attributes = new JSONObject();
            attributes.put("cm:title", "");
            DEFAULT_SCHEMA.put("attributes", attributes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Setter
    private SearchService searchService;
    @Setter
    private NamespaceService namespaceService;
    @Setter
    private NodeAttributeService attributeService;
    @Setter
    private NodeService nodeService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        String searchQuery = req.getParameter(PARAM_QUERY);
        if (searchQuery == null) {
            status.setCode(Status.STATUS_BAD_REQUEST, "Parameter '" + PARAM_QUERY + "' not specified");
            return null;
        }

        cache.setMaxAge((long) getInt(req, PARAM_CACHE_AGE, 0));

        String resultStr;
        try {
            resultStr = queryNodes(searchQuery, req).toString();
        } catch (JSONException e) {
            throw new AlfrescoRuntimeException("JSONException", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", resultStr);

        return result;
    }

    private JSONObject queryNodes(String searchQuery, WebScriptRequest req) throws JSONException {

        JSONObject result = new JSONObject();

        int maxItems = getInt(req, PARAM_MAX_ITEMS, DEFAULT_MAX_ITEMS);
        int skipCount = getInt(req, PARAM_SKIP_COUNT, DEFAULT_SKIP_COUNT);
        String language = getString(req, PARAM_LANGUAGE, DEFAULT_LANGUAGE);

        List<NodeRef> nodeRefs = queryNodes(searchQuery, language, maxItems, skipCount);
        JSONObject schema = getSchema(req.getParameter(PARAM_SCHEMA));

        List<JSONObject> nodesData = new ArrayList<>();
        for (NodeRef nodeRef : nodeRefs) {
            nodesData.add(formatNode(nodeRef, schema));
        }
        result.put("results", nodesData);

        JSONObject queryData = new JSONObject();
        queryData.put("language", language);
        queryData.put("value", searchQuery);
        result.put("query", queryData);

        JSONObject pageData = new JSONObject();
        pageData.put("maxItems", maxItems);
        pageData.put("skipCount", skipCount);
        pageData.put("totalCount", nodeRefs.size());
        result.put("paging", pageData);

        return result;
    }

    private List<NodeRef> queryNodes(String query, String language, int maxItems, int skipCount) {

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQuery(query);
        searchParameters.setLanguage(language);
        searchParameters.setMaxItems(maxItems);
        searchParameters.setSkipCount(skipCount);
        ResultSet resultSet = searchService.query(searchParameters);
        try {
            return resultSet.getNodeRefs();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    private JSONObject formatNode(NodeRef nodeRef, JSONObject schema) throws JSONException {

        JSONObject data = new JSONObject();

        if (schema.has("nodeRef")) {
            data.put("nodeRef", nodeRef.toString());
        }

        if (schema.has("type")) {
            QName type = nodeService.getType(nodeRef);
            data.put("type", type.toPrefixString(namespaceService));
        }

        if (schema.has("attributes")) {

            JSONObject schemaAttributes = schema.optJSONObject("attributes");
            Iterator keysIt = schemaAttributes.keys();

            JSONObject nodeAttributes = new JSONObject();

            while (keysIt.hasNext()) {

                String key = (String) keysIt.next();
                QName keyQName = QName.resolveToQName(namespaceService, key);

                Object nodeValue = attributeService.getAttribute(nodeRef, keyQName);
                Object schemaValue = schemaAttributes.opt(key);

                if (schemaValue instanceof JSONObject) {

                    JSONObject schemaObjValue = (JSONObject) schemaValue;
                    JSONObject value = null;

                    if (nodeValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<NodeRef> nodeValues = (List<NodeRef>) nodeValue;
                        if (!nodeValues.isEmpty()) {
                            JSONObject subNodesSchema = schemaObjValue.length() > 0 ? schemaObjValue : DEFAULT_SCHEMA;
                            value = formatNode(nodeValues.get(0), subNodesSchema);
                        }
                    }

                    nodeAttributes.put(key, value);

                } else if (schemaValue instanceof JSONArray) {

                    JSONArray schemaArray = (JSONArray) schemaValue;
                    JSONObject subNodesSchema = DEFAULT_SCHEMA;
                    if (schemaArray.length() > 0 && schemaArray.optJSONObject(0).length() > 0) {
                        subNodesSchema = schemaArray.optJSONObject(0);
                    }

                    List<JSONObject> value = new ArrayList<>();

                    if (nodeValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<NodeRef> nodeValues = (List<NodeRef>) nodeValue;
                        for (NodeRef node : nodeValues) {
                            value.add(formatNode(node, subNodesSchema));
                        }
                    }

                    nodeAttributes.put(key, value);

                } else if (nodeValue == null) {
                    nodeAttributes.put(key, JSONObject.NULL);
                } else {
                    nodeAttributes.put(key, String.valueOf(nodeValue));
                }
            }

            data.put("attributes", nodeAttributes);
        }
        return data;
    }

    private JSONObject getSchema(String schema) {
        try {
            return StringUtils.isNotBlank(schema) ? new JSONObject(schema) : DEFAULT_SCHEMA;
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
