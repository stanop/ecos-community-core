package ru.citeck.ecos.action.group;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActionExecution<T> {

    private Iterable<T> nodes;
    private GroupAction<T> action;

    private boolean cancelled = false;
    private AtomicBoolean started = new AtomicBoolean();

    private long startedTime;
    private long timeoutTime;
    private String author;

    ActionExecution(Iterable<T> nodes, GroupAction<T> action, String author) {
        this.nodes = nodes;
        this.action = action;
        this.author = author;
    }

    List<ActionResult<T>> run() {

        startedTime = System.currentTimeMillis();
        long timeout = action.getTimeout();
        timeoutTime = timeout > 0 ? startedTime + timeout : Long.MAX_VALUE;

        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Execution already started! " + toString());
        }

        for (T ref : nodes) {
            action.process(ref);
            if (cancelled) {
                break;
            }
            if (isTimeoutReached()) {
                throw new RuntimeException("Action timeout is reached. " + toString());
            }
        }
        return cancelled ? action.cancel() : action.complete();
    }

    public void cancel() {
        cancelled = true;
    }

    public long getStartedTime() {
        return startedTime;
    }

    public Date getStartedDate() {
        return new Date(startedTime);
    }

    public Date getTimeoutDate() {
        return new Date(timeoutTime);
    }

    public long getTimeoutTime() {
        return timeoutTime;
    }

    public long getExecutionTime() {
        if (started.get()) {
            return System.currentTimeMillis() - startedTime;
        } else {
            return 0;
        }
    }

    public boolean isStarted() {
        return started.get();
    }

    public GroupAction<T> getAction() {
        return action;
    }

    public boolean isTimeoutReached() {
        return started.get() && System.currentTimeMillis() > timeoutTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ActionExecution that = (ActionExecution) o;

        return nodes.equals(that.nodes) && action.equals(that.action);
    }

    @Override
    public int hashCode() {
        int result = nodes.hashCode();
        result = 31 * result + action.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("ActionExecution[" +
                             "action: %s, startedTime: %d, timeoutTime: %d, author: %s" +
                             "]", action, startedTime, timeoutTime, author);
    }
}
