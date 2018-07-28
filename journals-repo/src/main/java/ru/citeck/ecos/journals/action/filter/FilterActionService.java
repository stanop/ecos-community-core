package ru.citeck.ecos.journals.action.filter;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.journals.action.group.GroupActionResult;
import ru.citeck.ecos.search.SearchCriteria;

import java.util.Map;

/**
 * Service to execute some action with every node matched criteria
 *
 * @author Pavel Simonov
 */
public interface FilterActionService {

    Map<NodeRef, GroupActionResult> invoke(SearchCriteria searchCriteria,
                                           String journalId,
                                           String language,
                                           String actionId,
                                           Map<String, String> params);

    void register(FilterActionFactory executor);
}
