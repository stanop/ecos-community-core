package ru.citeck.ecos.behavior.activity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface CaseTaskAttributesConverter {

    /**
     * @param properties workflow properties
     * @param taskRef icaseTask:task instance reference
     * @return
     */
    Map<QName, Serializable> convert(Map<QName, Serializable> properties, NodeRef taskRef);

    /**
     * @return workflow types id. For example activiti$confirm or flowable$perform
     */
    Set<String> getWorkflowTypes();
}

