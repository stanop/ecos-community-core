package ru.citeck.ecos.flowable.temp;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

/**
 * Created by impi on 13.10.17.
 */
public abstract class AbstractExecutionListener extends AbstractListener implements ExecutionListener {

    @Override
    public final void notify(DelegateExecution execution) {
        init();
        notifyImpl(execution);
    }



    protected abstract void notifyImpl(DelegateExecution execution);
}
