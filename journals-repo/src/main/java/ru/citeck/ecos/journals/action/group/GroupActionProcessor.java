package ru.citeck.ecos.journals.action.group;

import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.concurrent.Future;

/**
 * @author Pavel Simonov
 */
public interface GroupActionProcessor {

    Future<GroupActionResult> process(RemoteNodeRef nodeRef);

    void end();
}
