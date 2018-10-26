package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceJS extends AlfrescoScopableProcessorExtension {

    private GroupActionService groupActionService;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ValueConverter converter = new ValueConverter();

    public ActionResultsJS execute(Iterable<?> nodes, Object config) {
        GroupActionConfig groupActionConfig = convertConfig(config, GroupActionConfig.class);
        return new ActionResultsJS(groupActionService.execute(nodes, groupActionConfig));
    }

    private static <T> T convertConfig(Object config, Class<T> type) {
        if (config == null) {
            return null;
        }
        Object configObj = converter.convertValueForJava(config);
        return objectMapper.convertValue(configObj, type);
    }

    @Autowired
    public void setGroupActionService(GroupActionService groupActionService) {
        this.groupActionService = groupActionService;
    }

    public static class ActionResultsJS {

        private final ActionResults impl;

        public ActionResultsJS(ActionResults<?> impl) {
            this.impl = impl;
        }

        public ActionResult[] getResults() {
            return toResultsArray(impl);
        }

        private static ActionResult[] toResultsArray(ActionResults<?> results) {
            ActionResult[] result = new ActionResult[results.getResults().size()];
            return results.getResults().toArray(result);
        }

        public JSONObject getCancelCause() {
            Throwable cause = impl.getCancelCause();
            if (cause == null) {
                return null;
            }
            JSONObject result = new JSONObject();
            result.put("message", cause.getMessage());
            result.put("type", cause.getClass().getSimpleName());
            return result;
        }
    }
}
