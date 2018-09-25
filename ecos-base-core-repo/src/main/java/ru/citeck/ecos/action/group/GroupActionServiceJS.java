package ru.citeck.ecos.action.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceJS extends AlfrescoScopableProcessorExtension {

    private GroupActionService groupActionService;

    private static ObjectMapper objectMapper = new ObjectMapper();
    public ActionResult[] execute(Iterable<?> nodes, GroupActionConfig config) {
        return toArray(groupActionService.execute(nodes, config));
    }

    private static ActionResult[] toArray(ActionResults<?> results) {
        ActionResult[] result = new ActionResult[results.getResults().size()];
        return results.getResults().toArray(result);
    }

    @Autowired
    public void setGroupActionService(GroupActionService groupActionService) {
        this.groupActionService = groupActionService;
    }
}
