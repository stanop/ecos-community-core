package ru.citeck.ecos.action.group;

import org.alfresco.repo.jscript.ValueConverter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.citeck.ecos.repo.RemoteRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceJS extends AlfrescoScopableProcessorExtension {

    private static final String CONFIG_BATCH_SIZE = "batchSize";

    private GroupActionService groupActionService;
    private ValueConverter converter = new ValueConverter();

    public GroupActionStatusesJS execute(Object nodes, String actionId, Object paramsObj) throws JSONException {

        List<RemoteRef> nodeRefs = toNodeRefList(nodes);
        Map<String, String> params = toStringMap(paramsObj);

        GroupActionConfig config = new GroupActionConfig();
        config.setParams(params);

        Map<RemoteRef, GroupActionResult> statuses = groupActionService.execute(nodeRefs, actionId, config);

        return new GroupActionStatusesJS(statuses, getScope(), serviceRegistry);
    }

    public void executeAsync(Object nodes, Consumer<RemoteRef> action, Object config) {

        List<RemoteRef> nodeRefs = toNodeRefList(nodes);
        Map<String, String> configMap = toStringMap(config);

        GroupActionConfig groupActionConfig = new GroupActionConfig();
        String batchSize = configMap.get(CONFIG_BATCH_SIZE);
        if (StringUtils.isNotBlank(batchSize)) {
            groupActionConfig.setBatchSize(Integer.parseInt(batchSize));
        }

        groupActionService.executeAsync(nodeRefs, action, groupActionConfig);
    }

    private List<RemoteRef> toNodeRefList(Object array) {
        Object jArray = converter.convertValueForJava(array);
        List<RemoteRef> result = new ArrayList<>();
        if (jArray instanceof List) {
            for (Object obj : (List) jArray) {
                result.add(JavaScriptImplUtils.getRemoteNodeRef(obj));
            }
        } else if (jArray instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jArray;
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(JavaScriptImplUtils.getRemoteNodeRef(jsonArray.opt(i)));
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
