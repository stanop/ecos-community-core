package ru.citeck.ecos.action.group;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Pavel Simonov
 */
public class ActionResults<T> {

    @Setter
    private List<ActionResult<T>> results;

    @Getter @Setter
    private int processedCount;

    @Getter @Setter
    private int errorsCount;

    @Getter @Setter
    private Throwable cancelCause;

    public ActionResults() {
    }

    public ActionResults(ActionResults<T> other) {
        results = new ArrayList<>(other.results);
        processedCount = other.processedCount;
        errorsCount = other.errorsCount;
        cancelCause = other.cancelCause;
    }

    public <K> ActionResults(ActionResults<K> other, Function<K, T> mapper) {

        results = other.results.stream().map(r ->
                new ActionResult<>(mapper.apply(r.getData()), r.getStatus())).collect(Collectors.toList());

        processedCount = other.processedCount;
        errorsCount = other.errorsCount;
        cancelCause = other.cancelCause;
    }

    public ActionResults<T> merge(ActionResults<T> results) {
        getResults().addAll(results.getResults());
        processedCount += results.processedCount;
        errorsCount += results.getErrorsCount();
        if (results.getCancelCause() != null) {
            cancelCause = results.getCancelCause();
        }
        return this;
    }

    public List<ActionResult<T>> getResults() {
        if (results == null) {
            results = new ArrayList<>();
        }
        return results;
    }

    @Override
    public String toString() {
        return "ActionResults{" +
                "results=" + results +
                ", processedCount=" + processedCount +
                ", errorsCount=" + errorsCount +
                '}';
    }
}
