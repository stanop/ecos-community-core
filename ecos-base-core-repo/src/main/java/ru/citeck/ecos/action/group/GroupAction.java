package ru.citeck.ecos.action.group;

import ru.citeck.ecos.action.group.impl.ResultsListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @author Pavel Simonov
 */
public interface GroupAction<T> extends ResultsListener<T>, Closeable {

    void process(T nodeId);

    List<ActionResult<T>> complete();

    List<ActionResult<T>> cancel();

    void onError(Throwable error);

    boolean isAsync();

    long getTimeout();

    void addListener(ResultsListener<T> listener);

    default void onProcessed(List<ActionResult<T>> results) {}

    default void close() throws IOException {}
}
