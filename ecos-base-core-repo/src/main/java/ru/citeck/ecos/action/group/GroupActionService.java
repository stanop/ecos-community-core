package ru.citeck.ecos.action.group;

import ru.citeck.ecos.repo.RemoteRef;

import java.util.Map;
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
    Map<RemoteRef, GroupActionResult> execute(Iterable<RemoteRef> nodeRefs, GroupAction action);

    /**
     * Raw group action execution
     * Warning! Transaction splitting is not performed by service. Is is responsibility of GroupAction
     */
    void executeAsync(Iterable<RemoteRef> nodeRefs, GroupAction action);

    Map<RemoteRef, GroupActionResult> execute(Iterable<RemoteRef> nodeRefs,
                                              Consumer<RemoteRef> action,
                                              GroupActionConfig config);

    void executeAsync(Iterable<RemoteRef> nodeRefs,
                      Consumer<RemoteRef> action,
                      GroupActionConfig config);

    Map<RemoteRef, GroupActionResult> execute(Iterable<RemoteRef> nodeRefs,
                                              Function<RemoteRef, GroupActionResult> action,
                                              GroupActionConfig config);

    void executeAsync(Iterable<RemoteRef> nodeRefs,
                      Function<RemoteRef, GroupActionResult> action,
                      GroupActionConfig config);

    Map<RemoteRef, GroupActionResult> execute(Iterable<RemoteRef> nodeRefs,
                                              String actionId,
                                              GroupActionConfig config);

    void executeAsync(Iterable<RemoteRef> nodeRefs,
                      String actionId,
                      GroupActionConfig config);

    void register(GroupActionFactory factory);

    void register(GroupActionExecutor executor);
}
