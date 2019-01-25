package ru.citeck.ecos.action.group;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.mozilla.javascript.Undefined;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JsUtils;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceJS extends AlfrescoScopableProcessorExtension {

    private static final Log logger = LogFactory.getLog(GroupActionServiceJS.class);

    private GroupActionService groupActionService;

    private JsUtils jsUtils;

    public ActionResultsJS execute(Object nodes, Object config) {
        Iterable<?> iterableNodes = (Iterable) jsUtils.toJava(nodes);
        GroupActionConfig groupActionConfig = jsUtils.toJava(config, GroupActionConfig.class);
        return new ActionResultsJS(groupActionService.execute(iterableNodes, groupActionConfig));
    }

    public ActionResultsJS forEach(Object nodes, JsFunction func) {
        return forEach(nodes, func, null);
    }

    public ActionResultsJS forEach(Object nodes, JsFunction func, Object config) {

        Object javaNodes = jsUtils.toJava(nodes);

        if (!(javaNodes instanceof Iterable)) {
            throw new IllegalArgumentException("Nodes is not iterable!");
        }

        GroupActionConfig groupActionConfig = jsUtils.toJava(config, GroupActionConfig.class);

        Function<Object, ActionStatus> jsFunc = node -> {

            Object result = jsUtils.toJava(func.apply(node));

            if (result == null || result instanceof Void || result instanceof Undefined) {
                return ActionStatus.ok();
            }
            if (result instanceof ActionStatus) {
                return (ActionStatus) result;
            }
            if (result instanceof String) {
                if ("undefined".equals(result)) {
                    return ActionStatus.ok();
                } else {
                    return new ActionStatus((String) result);
                }
            }
            if (result instanceof Map) {
                return jsUtils.toJava(result, ActionStatus.class);
            }
            return ActionStatus.ok(result);
        };

        ActionResults<Object> results = groupActionService.execute((Iterable<Object>) javaNodes,
                                                                   jsFunc,
                                                                   groupActionConfig);

        if (results.getCancelCause() != null) {
            logger.error("Action was cancelled", results.getCancelCause());
        }

        return new ActionResultsJS(results);
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

        @Override
        public String toString() {
            return "ActionResultsJS{" + impl + '}';
        }
    }

    @Autowired
    public void setJsUtils(JsUtils jsUtils) {
        this.jsUtils = jsUtils;
    }

    public interface JsFunction {

        Object apply(Object argument);
    }
}
