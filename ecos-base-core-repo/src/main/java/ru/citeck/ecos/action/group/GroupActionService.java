package ru.citeck.ecos.action.group;

import ru.citeck.ecos.action.group.impl.GroupActionExecutor;

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
    <T> List<ActionResult<T>> execute(Iterable<T> nodes,
                                      GroupAction<T> action);

    <T> List<ActionResult<T>> execute(Iterable<T> nodes,
                                      Consumer<T> action,
                                      GroupActionConfig config);

    <T> List<ActionResult<T>> execute(Iterable<T> nodes,
                                      Function<T, ActionStatus> action,
                                      GroupActionConfig config);

    <T> List<ActionResult<T>> execute(Iterable<T> nodes,
                                      String actionId,
                                      GroupActionConfig config);

    <T> GroupAction<T> createAction(String actionId, GroupActionConfig config);

    <T> Class<T> getActionType(String actionId);

    void cancelActions();

    List<ActionExecution<?>> getActiveActions();

    void register(GroupActionFactory factory);

    void register(GroupActionExecutor executor);
}
