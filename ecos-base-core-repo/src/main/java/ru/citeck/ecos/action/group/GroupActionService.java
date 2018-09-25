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
    <T> ActionResults<T> execute(Iterable<T> nodes,
                                 GroupAction<T> action);

    <T> ActionResults<T> execute(Iterable<T> nodes,
                                 Consumer<T> action,
                                 GroupActionConfig config);

    <T> ActionResults<T> execute(Iterable<T> nodes,
                                 Function<T, ActionStatus> action,
                                 GroupActionConfig config);

    <T> ActionResults<T> execute(Iterable<T> nodes,
                                 GroupActionConfig config);

    <T> GroupAction<T> createAction(GroupActionConfig config);

    void cancelAllActions();

    List<ActionExecution<?>> getActiveActions();

    void register(GroupActionFactory factory);

    void register(GroupActionExecutor executor);
}
