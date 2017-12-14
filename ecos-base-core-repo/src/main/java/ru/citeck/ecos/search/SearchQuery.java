package ru.citeck.ecos.search;

import lombok.Setter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.citeck.ecos.attr.NodeAttributeService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for search-query and batch-search-query webscripts.
 * @see <a href="alfresco/templates/webscripts/ru/citeck/search/search-query.get.desc.xml">/citeck/search/query</a> and
 * <a href="alfresco/templates/webscripts/ru/citeck/search/batch-search-query.post.desc.xml">/citeck/search/batch-query</a>
 * webscripts
 */
public class SearchQuery {

    public static final int DEFAULT_MAX_ITEMS = 1000;
    public static final int DEFAULT_SKIP_COUNT = 0;
    public static final String DEFAULT_LANGUAGE = SearchService.LANGUAGE_FTS_ALFRESCO;
    public static final JSONObject DEFAULT_SCHEMA = new JSONObject();
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


    public JSONObject queryNodes(String searchQuery) throws JSONException {
        return queryNodes(searchQuery, DEFAULT_MAX_ITEMS, DEFAULT_SKIP_COUNT, DEFAULT_LANGUAGE, DEFAULT_SCHEMA);
    }

    public JSONObject queryNodes(String searchQuery, int maxItems, int skipCount, String language, JSONObject schema) throws JSONException {

        JSONObject result = new JSONObject();

        List<NodeRef> nodeRefs = queryNodes(searchQuery, language, maxItems, skipCount);

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
}
