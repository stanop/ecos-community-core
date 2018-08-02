package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.alfresco.repo.jscript.ValueConverter;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.citeck.ecos.repo.RemoteRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceJS extends AlfrescoScopableProcessorExtension {

    private GroupActionService groupActionService;
    private ValueConverter converter = new ValueConverter();
    private ObjectMapper objectMapper = new ObjectMapper();

    public ActionResult[] execute(Object nodes, String actionId, Object config) {
        Iterable<RemoteRef> nodeRefs = toIterableNodes(nodes);
        return toArray(groupActionService.execute(nodeRefs, actionId, parseConfig(config)));
    }

    public ActionResult[] execute(Object nodes, Consumer<RemoteRef> action) {
        return execute(nodes, action, null);
    }

    public ActionResult[] execute(Object nodes, Consumer<RemoteRef> action, Object config) {
        Iterable<RemoteRef> nodeRefs = toIterableNodes(nodes);
        return toArray(groupActionService.execute(nodeRefs, action, parseConfig(config)));
    }

    public ActionExecution[] getActiveActions() {
        List<ActionExecution> executions = groupActionService.getActiveActions();
        return executions.toArray(new ActionExecution[executions.size()]);
    }

    public void cancelActions() {
        groupActionService.cancelActions();
    }

    private GroupActionConfig parseConfig(Object config) {
        if (config == null) {
            return null;
        }
        Object configObj = converter.convertValueForJava(config);
        return objectMapper.convertValue(configObj, GroupActionConfig.class);
    }

    private ActionResult[] toArray(List<ActionResult> results) {
        return results.toArray(new ActionResult[results.size()]);
    }

    private Iterable<RemoteRef> toIterableNodes(Object nodes) {
        Object jNodes = converter.convertValueForJava(nodes);
        List<RemoteRef> resultList;
        if (jNodes instanceof List) {
            resultList = new ArrayList<>();
            for (Object obj : (List) jNodes) {
                resultList.add(JavaScriptImplUtils.getRemoteNodeRef(obj));
            }
        } else if (jNodes instanceof JSONArray) {
            resultList = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) jNodes;
            for (int i = 0; i < jsonArray.length(); i++) {
                resultList.add(JavaScriptImplUtils.getRemoteNodeRef(jsonArray.opt(i)));
            }
        } else if (jNodes instanceof Iterable) {
            @SuppressWarnings("unchecked")
            Iterable<RemoteRef> iterableNodes = (Iterable<RemoteRef>) jNodes;
            return Iterables.transform(iterableNodes, JavaScriptImplUtils::getRemoteNodeRef);
        } else {
            resultList = Collections.emptyList();
        }
        return resultList;
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
