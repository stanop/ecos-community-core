package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.jscript.ValueConverter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceJS extends AlfrescoScopableProcessorExtension {

    private GroupActionService groupActionService;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ValueConverter converter = new ValueConverter();

    public ActionResult[] execute(Iterable<?> nodes, Object config) {
        GroupActionConfig groupActionConfig = convertConfig(config, GroupActionConfig.class);
        return toArray(groupActionService.execute(nodes, groupActionConfig));
    }

    private static ActionResult[] toArray(ActionResults<?> results) {
        ActionResult[] result = new ActionResult[results.getResults().size()];
        return results.getResults().toArray(result);
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
}
