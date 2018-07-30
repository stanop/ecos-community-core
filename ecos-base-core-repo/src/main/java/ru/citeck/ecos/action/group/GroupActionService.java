package ru.citeck.ecos.action.group;

import ru.citeck.ecos.repo.RemoteNodeRef;

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
    Map<RemoteNodeRef, GroupActionResult> execute(Iterable<RemoteNodeRef> nodeRefs, GroupAction action);

    /**
     * Raw group action execution
     * Warning! Transaction splitting is not performed by service. Is is responsibility of GroupAction
     */
    void executeAsync(Iterable<RemoteNodeRef> nodeRefs, GroupAction action);

    Map<RemoteNodeRef, GroupActionResult> execute(Iterable<RemoteNodeRef> nodeRefs,
                                                  Consumer<RemoteNodeRef> action,
                                                  GroupActionConfig config);

    void executeAsync(Iterable<RemoteNodeRef> nodeRefs,
                      Consumer<RemoteNodeRef> action,
                      GroupActionConfig config);

    Map<RemoteNodeRef, GroupActionResult> execute(Iterable<RemoteNodeRef> nodeRefs,
                                                  Function<RemoteNodeRef, GroupActionResult> action,
                                                  GroupActionConfig config);

    void executeAsync(Iterable<RemoteNodeRef> nodeRefs,
                      Function<RemoteNodeRef, GroupActionResult> action,
                      GroupActionConfig config);

    Map<RemoteNodeRef, GroupActionResult> execute(Iterable<RemoteNodeRef> nodeRefs,
                                                  String actionId,
                                                  GroupActionConfig config);

    void executeAsync(Iterable<RemoteNodeRef> nodeRefs,
                      String actionId,
                      GroupActionConfig config);

    void register(GroupActionFactory factory);

    void register(GroupActionExecutor executor);
}
