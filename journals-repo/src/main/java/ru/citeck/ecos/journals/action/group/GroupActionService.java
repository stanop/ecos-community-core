package ru.citeck.ecos.journals.action.group;

import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.List;
import java.util.Map;

/**
 * Service to execute some action with every node in group
 *
 * @author Pavel Simonov
 */
public interface GroupActionService {

    Map<RemoteNodeRef, GroupActionResult> invoke(List<RemoteNodeRef> nodeRefs,
                                                 String actionId,
                                                 Map<String, String> params);

    void invoke(String query,
                String journalId,
                String language,
                String actionId,
                Map<String, String> params);

    void register(GroupActionProcFactory factory);
}
