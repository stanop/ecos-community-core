package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public class BpmModel {
    public static final String BPM_NAMESPASE = "http://www.alfresco.org/model/bpm/1.0";

    public static final QName TYPE_BPM_PACKAGE = QName.createQName(BPM_NAMESPASE, "package");
    public static final QName PROPERTY_TASK_ID = QName.createQName(BPM_NAMESPASE, "taskId");
}
