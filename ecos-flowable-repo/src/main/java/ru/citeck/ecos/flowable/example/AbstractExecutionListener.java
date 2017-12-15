package ru.citeck.ecos.flowable.example;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

/**
 * Abstract execution listener
 */
public abstract class AbstractExecutionListener extends AbstractListener implements ExecutionListener {

    @Override
    public final void notify(DelegateExecution execution) {
        init();
        notifyImpl(execution);
    }

    protected abstract void notifyImpl(DelegateExecution execution);
}
