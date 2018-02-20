package ru.citeck.ecos.flowable.listeners.global.impl.task.complete;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.flowable.task.service.delegate.DelegateTask;
import ru.citeck.ecos.flowable.listeners.global.GlobalCompleteTaskListener;

/**
 * Task outcome push listener
 */
public class TaskOutcomePushListener implements GlobalCompleteTaskListener {

    /**
     * Outcome property name
     */
    private String outcomePropertyName;

    /**
     * Namespace prefix resolver
     */
    private NamespacePrefixResolver namespaceService;

    /**
     * Notify
     * @param delegateTask Task
     */
    @Override
    public void notify(DelegateTask delegateTask) {

        QName propertyQName = (QName) delegateTask.getVariable("bpm_outcomePropertyName");
        if(propertyQName == null) {
            return;
        }

        String propertyName = propertyQName.toPrefixString(namespaceService).replace(":", "_");
        Object outcome = delegateTask.getVariable(propertyName);
        delegateTask.setVariable(outcomePropertyName, outcome);
    }

    /**
     * Set outcome property name
     * @param outcomePropertyName Outcome property name
     */
    public void setOutcomePropertyName(String outcomePropertyName) {
        this.outcomePropertyName = outcomePropertyName;
    }

    /**
     * Set namespace prefix resolver
     * @param namespaceService Namespace prefix resolver
     */
    public void setNamespaceService(NamespacePrefixResolver namespaceService) {
        this.namespaceService = namespaceService;
    }
}
