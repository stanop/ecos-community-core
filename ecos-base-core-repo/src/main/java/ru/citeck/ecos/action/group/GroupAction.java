package ru.citeck.ecos.action.group;

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
}
