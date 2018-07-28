package ru.citeck.ecos.journals.action.filter;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Map;

public interface FilterActionFactory {

    FilterActionEvaluator begin();

    void execute(NodeRef nodeRef, Map<String, String> params);

    String getActionId();

    String getInstance();
}
