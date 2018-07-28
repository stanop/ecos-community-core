package ru.citeck.ecos.journals.action;

import ru.citeck.ecos.journals.action.group.GroupActionResult;
import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.concurrent.Future;

/**
 * @author Pavel Simonov
 */
public interface GroupActionProcessor {

    Future<GroupActionResult> process(RemoteNodeRef nodeRef);

    void end();
}
