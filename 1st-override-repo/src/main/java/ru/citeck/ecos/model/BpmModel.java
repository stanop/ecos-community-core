package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public class BpmModel {
    public static final String BPM_NAMESPASE = "http://www.alfresco.org/model/bpm/1.0";

    public static final QName TYPE_BPM_PACKAGE = QName.createQName(BPM_NAMESPASE, "package");
    public static final QName PROPERTY_TASK_ID = QName.createQName(BPM_NAMESPASE, "taskId");
    public static final QName PROPERTY_COMMENT = QName.createQName(BPM_NAMESPASE, "comment");
    public static final QName PROPERTY_DESCRIPTION = QName.createQName(BPM_NAMESPASE, "description");
    public static final QName PROPERTY_STATUS = QName.createQName(BPM_NAMESPASE, "status");

    public static final QName TYPE_APPROVAL_TOKEN = QName.createQName(BPM_NAMESPASE, "approvalToken");
    public static final QName PROPERTY_TOKEN_VALUE = QName.createQName(BPM_NAMESPASE, "tokenValue");
    public static final QName PROPERTY_TOKEN_AUTHORITY_EMAIL = QName.createQName(BPM_NAMESPASE, "tokenAuthorityEmail");

    public static final QName ASSOC_BPM_APPROVAL_TOKENS = QName.createQName(BPM_NAMESPASE, "approvalTokens");
}
