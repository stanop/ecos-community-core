package ru.citeck.ecos.eform.model;

import org.alfresco.service.namespace.QName;

public final class EcosEformFileModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/eform-file/1.0";

    public static final QName TYPE_TEMP_FILE = QName.createQName(NAMESPACE, "tempFile");

    public static final QName PROP_TEMP_FILE_ID = QName.createQName(NAMESPACE, "tempFileId");

}