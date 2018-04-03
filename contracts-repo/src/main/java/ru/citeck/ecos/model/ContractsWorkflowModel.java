package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * @author Anton Ivanov
 */
public interface ContractsWorkflowModel {

    String NAMESPACE = "http://www.citeck.ru/model/contracts/workflow/1.0";

    QName TYPE_SELECT_SIGNER_TASK = QName.createQName(NAMESPACE, "selectSignerTask");
    QName TYPE_SIGN_COUNTERPARTY_TASK = QName.createQName(NAMESPACE, "signCounterpartyTask");

    QName PROP_CONTRACT_LINKS = QName.createQName(NAMESPACE, "contractLinks");
    QName PROP_RKK_LINKS = QName.createQName(NAMESPACE, "rkkLinks");

    QName ASSOC_LINKED_CONTRACTS = QName.createQName(NAMESPACE, "linkedContracts");
    QName ASSOC_LINKED_RKK = QName.createQName(NAMESPACE, "linkedRKK");
}
