package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface ContractsModel {
    
    String NAMESPACE = "http://www.citeck.ru/model/contracts/1.0";

    public static final QName CONTRACTS_TYPE = QName.createQName(NAMESPACE, "agreement");
    public static final QName CONTRACTS_SUPPLEMENTARY_TYPE = QName.createQName(NAMESPACE, "supplementaryAgreement");
    public static final QName TYPE_CONTRACTS_CLOSING_DOCUMENT = QName.createQName(NAMESPACE, "closingDocument");

    public static final QName ASSOC_CONTRACTOR = QName.createQName(NAMESPACE, "contractor");
    public static final QName ASSOC_CLOSING_DOCUMENT_AGREEMENT = QName.createQName(NAMESPACE, "closingDocumentAgreement");
    public static final QName ASSOC_CLOSING_DOCUMENT_CURRENCY = QName.createQName(NAMESPACE, "closingDocumentCurrency");
    public static final QName ASSOC_AGREEMENT_CURRENCY = QName.createQName(NAMESPACE, "agreementCurrency");
    public static final QName ASSOC_CLOSING_DOCUMENT_PAYMENT = QName.createQName(NAMESPACE, "closingDocumentPayment");

    public static final QName PROP_CLOSING_DOCUMENT_AMOUNT = QName.createQName(NAMESPACE, "closingDocumentAmount");
    public static final QName PROP_CLOSING_DOCUMENT_AMOUNT_IN_WORDS = QName.createQName(NAMESPACE, "closingDocumentAmountInWords");

    public static final QName ASPECT_IS_CONTRACT_ATTACHMENT = QName.createQName(NAMESPACE, "isContractAttachment");
    public static final QName ASPECT_HAS_CONTRACTOR = QName.createQName(NAMESPACE, "hasContractor");
    
}