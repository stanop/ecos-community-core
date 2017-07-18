package ru.citeck.ecos.journals.group;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceJS extends AlfrescoScopableProcessorExtension {

    private GroupActionService groupActionService;
    private ValueConverter converter = new ValueConverter();

    public Map<ScriptNode, GroupActionStatus> invoke(Object nodes, String actionId, Object paramsObj) {
        List<NodeRef> nodeRefs = toNodeRefList(nodes);
        Map<String, String> params = toStringMap(paramsObj);

        Map<NodeRef, GroupActionStatus> status = groupActionService.invoke(nodeRefs, actionId, params);
        return toScriptStatus(status);
    }

    private Map<ScriptNode, GroupActionStatus> toScriptStatus(Map<NodeRef, GroupActionStatus> status) {
        Map<ScriptNode, GroupActionStatus> result = new HashMap<>();
        for (NodeRef nodeRef : status.keySet()) {
            ScriptNode node = new ScriptNode(nodeRef, getServiceRegistry(), getScope());
            result.put(node, status.get(nodeRef));
        }
        return result;
    }

    private List<NodeRef> toNodeRefList(Object array) {
        Object jArray = converter.convertValueForJava(array);
        List<NodeRef> result = new ArrayList<>();
        if (jArray instanceof List) {
            @SuppressWarnings("unchecked")
            List<NodeRef> jList = (List<NodeRef>) jArray;
            result.addAll(jList);
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
