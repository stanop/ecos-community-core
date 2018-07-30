package ru.citeck.ecos.action.group;

import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.concurrent.Future;

/**
 * @author Pavel Simonov
 */
@FunctionalInterface
public interface GroupAction {

    Future<GroupActionResult> process(RemoteNodeRef nodeRef);

    default void end() {}
}
