package ru.citeck.ecos.flowable.temp;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.flowable.engine.delegate.DelegateExecution;

/**
 * Confirm version js service
 */
public class FlowableConfirmableVersionJS extends BaseScopableProcessorExtension {

    private FlowableConfirmHelper impl;

    public void saveConfirmable(DelegateExecution execution) {
        impl.saveConfirmableVersion(execution);
    }

    public void saveCurrent(DelegateExecution execution) {
        impl.saveCurrentVersion(execution);
    }

    public boolean isConfirmable(DelegateExecution execution) {
        return !impl.isConfirmableVersion(execution);
    }

    public boolean isLatestVersionConfirmedByAll(DelegateExecution execution) {
        return !impl.isLatestVersionConfirmedByAll(execution);
    }

    public boolean isChanged(DelegateExecution execution) {
        return !impl.isCurrentVersion(execution);
    }

    public FlowableConfirmHelper getImpl() {
        return impl;
    }

    public void setImpl(FlowableConfirmHelper impl) {
        this.impl = impl;
    }
}
