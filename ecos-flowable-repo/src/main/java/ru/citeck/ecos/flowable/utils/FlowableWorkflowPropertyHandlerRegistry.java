package ru.citeck.ecos.flowable.utils;

import org.alfresco.repo.workflow.WorkflowPropertyHandler;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Flowable workflow property handler registry
 */
public class FlowableWorkflowPropertyHandlerRegistry {
    /**
     * Handlers
     */
    private final Map<QName, WorkflowPropertyHandler> handlers = new HashMap<QName, WorkflowPropertyHandler>();

    /**
     * Default handler
     */
    private final WorkflowPropertyHandler defaultHandler;

    /**
     * Qname converter
     */
    private final WorkflowQNameConverter qNameConverter;

    /**
     * Constructor
     * @param defaultHandler WorkflowPropertyHandler
     * @param qNameConverter WorkflowQNameConverter
     */
    public FlowableWorkflowPropertyHandlerRegistry(WorkflowPropertyHandler defaultHandler, WorkflowQNameConverter qNameConverter)
    {
        this.defaultHandler = defaultHandler;
        this.qNameConverter = qNameConverter;
    }

    /**
     * Register handler
     * @param key Key
     * @param handler Handler
     */
    public void registerHandler(QName key, WorkflowPropertyHandler handler)
    {
        handlers.put(key, handler);
    }

    /**
     * Clear
     */
    public void clear()
    {
        handlers.clear();
    }

    /**
     * Handle variables to set
     * @param properties Properties
     * @param type Type
     * @param object Object
     * @param objectType Object type
     * @return Variables
     */
    public Map<String, Object> handleVariablesToSet(Map<QName, Serializable> properties,
                                                    TypeDefinition type,
                                                    Object object, Class<?> objectType) {
        Map<String, Object> variablesToSet = new HashMap<String, Object>();
        for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
            QName key = entry.getKey();
            Serializable value = entry.getValue();
            WorkflowPropertyHandler handler = handlers.get(key);
            if (handler == null) {
                handler = defaultHandler;
            }
            Object result = null;
            if (type != null) {
                result = handler.handleProperty(key, value, type, object, objectType);
            } else {
                result = value;
            }
            if (WorkflowPropertyHandler.DO_NOT_ADD.equals(result)==false) {
                String keyStr = qNameConverter.mapQNameToName(key);
                variablesToSet.put(keyStr, result);
            }
        }
        return variablesToSet;
    }
}
