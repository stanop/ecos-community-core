package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * BPM Package model
 */
public class BpmPackageModel {

    public static final String NAMESPACE = "http://www.alfresco.org/model/bpm/1.0";

    public static final QName TYPE = QName.createQName(NAMESPACE, "package");

    public static final QName PROP_WORKFLOW_INSTANCE_ID = QName.createQName(NAMESPACE, "workflowInstanceId");
    public static final QName PROP_WORKFLOW_DEFINITION_NAME = QName.createQName(NAMESPACE, "workflowDefinitionName");
    public static final QName PROP_IS_SYSTEM_PACKAGE = QName.createQName(NAMESPACE, "isSystemPackage");

    public static final QName ASSOC_PACKAGE_CONTAINS = QName.createQName(NAMESPACE, "packageContains");
}
