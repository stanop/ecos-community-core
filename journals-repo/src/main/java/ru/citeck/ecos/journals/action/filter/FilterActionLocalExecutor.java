package ru.citeck.ecos.journals.action.filter;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.journals.action.group.GroupActionExecutor;

import java.util.Map;

public abstract class FilterActionLocalExecutor implements FilterActionExecutor {

    private GroupActionExecutor executor;

    @Override
    public void execute(NodeRef nodeRef, Map<String, String> params) {

        executor.invoke(nodeRef, params);

    }

    @Override
    public String getActionId() {
        return null;
    }

    @Override
    public String getInstance() {
        return null;
    }
}
