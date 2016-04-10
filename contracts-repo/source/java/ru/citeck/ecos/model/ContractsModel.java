package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface ContractsModel {
    
    String NAMESPACE = "http://www.citeck.ru/model/contracts/1.0";

    QName TYPE_CONTRACTS_CLOSING_DOCUMENT = QName.createQName(NAMESPACE, "closingDocument");

    QName ASSOC_CONTRACTOR = QName.createQName(NAMESPACE, "contractor");
    QName ASSOC_CLOSING_DOCUMENT_AGREEMENT = QName.createQName(NAMESPACE, "closingDocumentAgreement");
    
}