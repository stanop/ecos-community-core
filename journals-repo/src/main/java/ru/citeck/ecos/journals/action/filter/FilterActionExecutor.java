package ru.citeck.ecos.journals.action.filter;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.journals.action.group.GroupActionExecutor;

import java.util.Map;

public interface FilterActionExecutor {

    void execute(NodeRef nodeRef, Map<String, String> params);

    String getActionId();

    String getInstance();
}
