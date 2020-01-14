package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface ContractsModel {

    String NAMESPACE = "http://www.citeck.ru/model/contracts/1.0";

    QName CONTRACTS_TYPE = QName.createQName(NAMESPACE, "agreement");
    QName CONTRACTS_SUPPLEMENTARY_TYPE = QName.createQName(NAMESPACE, "supplementaryAgreement");
    QName TYPE_CONTRACTS_CLOSING_DOCUMENT = QName.createQName(NAMESPACE, "closingDocument");

    QName ASSOC_CONTRACTOR = QName.createQName(NAMESPACE, "contractor");
    QName ASSOC_CLOSING_DOCUMENT_AGREEMENT = QName.createQName(NAMESPACE, "closingDocumentAgreement");
    QName ASSOC_CLOSING_DOCUMENT_CURRENCY = QName.createQName(NAMESPACE, "closingDocumentCurrency");
    QName ASSOC_AGREEMENT_CURRENCY = QName.createQName(NAMESPACE, "agreementCurrency");
    QName ASSOC_CLOSING_DOCUMENT_PAYMENT = QName.createQName(NAMESPACE, "closingDocumentPayment");

    QName PROP_CLOSING_DOCUMENT_AMOUNT = QName.createQName(NAMESPACE, "closingDocumentAmount");
    QName PROP_CLOSING_DOCUMENT_AMOUNT_IN_WORDS = QName.createQName(NAMESPACE, "closingDocumentAmountInWords");

    QName ASPECT_IS_CONTRACT_ATTACHMENT = QName.createQName(NAMESPACE, "isContractAttachment");
    QName ASPECT_HAS_CONTRACTOR = QName.createQName(NAMESPACE, "hasContractor");

}
