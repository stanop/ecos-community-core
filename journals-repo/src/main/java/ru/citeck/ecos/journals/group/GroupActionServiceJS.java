package ru.citeck.ecos.journals.group;

import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceJS extends AlfrescoScopableProcessorExtension {

    public static final String BATCH_PARAM_KEY = "evaluateBatch";


    private GroupActionService groupActionService;
    private ValueConverter converter = new ValueConverter();

    public GroupActionStatusesJS invoke(Object nodes, String actionId, Object paramsObj) throws JSONException {
        List<NodeRef> nodeRefs = toNodeRefList(nodes);
        Map<String, String> params = toStringMap(paramsObj);
        Map<NodeRef, GroupActionResult> statuses;

        if (StringUtils.isNotEmpty(params.get(BATCH_PARAM_KEY)) && params.get(BATCH_PARAM_KEY).equals("true")) {
            statuses = groupActionService.invokeBatch(nodeRefs, actionId, params);
        } else {
            statuses = groupActionService.invoke(nodeRefs, actionId, params);
        }

        return new GroupActionStatusesJS(statuses, getScope(), serviceRegistry);
    }

    private List<NodeRef> toNodeRefList(Object array) {
        Object jArray = converter.convertValueForJava(array);
        List<NodeRef> result = new ArrayList<>();
        if (jArray instanceof List) {
            for (Object obj : (List) jArray) {
                result.add(JavaScriptImplUtils.getNodeRef(obj));
            }
        } else if (jArray instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jArray;
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(JavaScriptImplUtils.getNodeRef(jsonArray.opt(i)));
            }
        }
        return result;
    }

    private Map<String, String> toStringMap(Object map) {
        Map<String, String> result = new HashMap<>();
        Object jMap = converter.convertValueForJava(map);
        if (jMap instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> jStringMap = (Map<String, String>) map;
            result.putAll(jStringMap);
        } else if (jMap instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) jMap;
            Iterator keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = jsonObject.optString(key);
                result.put(key, value);
            }
        }
        return result;
    }

    public void setGroupActionService(GroupActionService groupActionService) {
        this.groupActionService = groupActionService;
    }
}
