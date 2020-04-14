package ru.citeck.ecos.behavior.activity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.icase.activity.dto.ActivityInstance;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface CaseTaskAttributesConverter {

    /**
     * @param properties workflow properties
     * @param taskRef icaseTask:task instance reference
     */
    Map<QName, Serializable> convert(Map<QName, Serializable> properties, NodeRef taskRef);

    /**
     * @param properties workflow properties
     * @param instance activity instance with type 'task'
     */
    Map<QName, Serializable> convert(Map<QName, Serializable> properties, NodeRef caseRef, ActivityInstance instance);

    /**
     * @return workflow types id. For example activiti$confirm or flowable$perform
     */
    Set<String> getWorkflowTypes();
}

