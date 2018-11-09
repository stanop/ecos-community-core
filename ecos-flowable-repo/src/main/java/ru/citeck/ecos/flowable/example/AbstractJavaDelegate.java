package ru.citeck.ecos.flowable.example;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * Abstract java delegate with providing service registry.
 *
 * @author Roman Makarskiy
 */
public abstract class AbstractJavaDelegate extends AbstractServiceProvider implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        init();
        executeImpl(execution);
    }

    protected abstract void executeImpl(DelegateExecution execution);
}
