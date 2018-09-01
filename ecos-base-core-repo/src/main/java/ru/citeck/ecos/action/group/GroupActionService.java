package ru.citeck.ecos.action.group;

import ru.citeck.ecos.action.group.impl.GroupActionExecutor;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Service to execute some action with every node in group
 *
 * @author Pavel Simonov
 */
public interface GroupActionService {

    /**
     * Raw group action execution
     * Warning! Transaction splitting is not performed by service. Is is responsibility of GroupAction
     */
    List<ActionResult> execute(Iterable<RemoteRef> nodes,
                               GroupAction action);

    List<ActionResult> execute(Iterable<RemoteRef> nodes,
                               Consumer<RemoteRef> action,
                               GroupActionConfig config);

    List<ActionResult> execute(Iterable<RemoteRef> nodes,
                               Function<RemoteRef, ActionStatus> action,
                               GroupActionConfig config);

    List<ActionResult> execute(Iterable<RemoteRef> nodes,
                               String actionId,
                               GroupActionConfig config);

    void cancelActions();

    List<ActionExecution> getActiveActions();

    void register(GroupActionFactory factory);

    void register(GroupActionExecutor executor);
}
