package ru.citeck.ecos.action.group;

import ru.citeck.ecos.action.group.impl.ResultsListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @author Pavel Simonov
 */
public interface GroupAction<T> extends ResultsListener<T>, Closeable {

    boolean process(T nodeId);

    ActionResults<T> complete();

    ActionResults<T> cancel(Throwable cause);

    boolean isAsync();

    long getTimeout();

    boolean isReadOnly();

    void addListener(ResultsListener<T> listener);

    default void onProcessed(List<ActionResult<T>> results) {}

    default void close() throws IOException {}
}
