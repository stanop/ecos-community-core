package ru.citeck.ecos.icase.completeness.current;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Set;

public interface CurrentLevelsResolver {

    Set<NodeRef> getCurrentLevels(NodeRef caseNode);

    int getOrder();
}
