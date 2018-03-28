package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * Document final status model
 */
public class IdocsFinalStatusModel {

    // model
    public static final String IDOCS_MODEL_PREFIX = "idocs";

    // namespace
    public static final String IDOCS_NAMESPACE = "http://www.citeck.ru/model/content/idocs/1.0";

    // types
    public static final QName TYPE_DOC_FINAL_STATUS = QName.createQName(IDOCS_NAMESPACE, "documentFinalStatus");

    // properties
    public static final QName PROP_DOC_TYPE = QName.createQName(IDOCS_NAMESPACE, "documentType");

    //assocs
    public static final QName ASSOC_FINAL_STATUSES = QName.createQName(IDOCS_NAMESPACE, "finalStatuses");
}
