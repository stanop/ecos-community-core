package ru.citeck.ecos.action.group;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

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
