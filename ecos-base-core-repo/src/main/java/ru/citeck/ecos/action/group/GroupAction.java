package ru.citeck.ecos.action.group;

import ru.citeck.ecos.repo.RemoteRef;

import java.util.concurrent.Future;

/**
 * @author Pavel Simonov
 */
@FunctionalInterface
public interface GroupAction {

    Future<GroupActionResult> process(RemoteRef nodeRef);

    default void end() {}
}
