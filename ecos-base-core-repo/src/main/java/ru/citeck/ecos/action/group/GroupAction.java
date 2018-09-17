package ru.citeck.ecos.action.group;

import ru.citeck.ecos.action.group.impl.ResultsListener;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public interface GroupAction<T> {

    void process(T nodeId);

    List<ActionResult<T>> complete();

    List<ActionResult<T>> cancel();

    boolean isAsync();

    long getTimeout();

    void addListener(ResultsListener<T> listener);
}
