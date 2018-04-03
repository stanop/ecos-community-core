package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public class SecurityWorkflowModel {

	public enum ProcessingStatus {
		NOT_PROCESSED,
		IN_PROGRESS,
		PARTIALLY_PROCESSED,
		FULLY_PROCESSED
	}

	//Namespaces
	public static final String SAMWF_NAMESPACE = "http://www.citeck.ru/model/samwf/1.0";

	//Types
	public static final QName TYPE_INCOME_PACKAGE_TASK = QName.createQName(SAMWF_NAMESPACE, "incomePackageTask");
	public static final QName TYPE_REJECTION_SIGN_TASK = QName.createQName(SAMWF_NAMESPACE, "rejectionSignTask");

	//Aspects

	//Properties
	public static final QName PROP_REGISTRATION_DATE = QName.createQName(SAMWF_NAMESPACE, "registrationDate");
	public static final QName PROP_ID_EDI = QName.createQName(SAMWF_NAMESPACE, "idEdi");
	public static final QName PROP_SIGNED_CONTRACTOR = QName.createQName(SAMWF_NAMESPACE, "signedContractor");
	public static final QName PROP_PROCESSING_STATUS = QName.createQName(SAMWF_NAMESPACE, "processingStatus");
	public static final QName PROP_DIADOC_TYPE = QName.createQName(SAMWF_NAMESPACE, "diadocType");
	public static final QName PROP_CASE_TYPE = QName.createQName(SAMWF_NAMESPACE, "caseType");
	public static final QName PROP_REG_NUMBER = QName.createQName(SAMWF_NAMESPACE, "regNumber");
	public static final QName PROP_INCOME_PACKAGE_TASK_ID = QName.createQName(SAMWF_NAMESPACE, "incomePackageTaskId");
	public static final QName PROP_REJECTION_SIGN_TASK_ID = QName.createQName(SAMWF_NAMESPACE, "rejectionSignTaskId");
	public static final QName PROP_CONTRACTOR_INN = QName.createQName(SAMWF_NAMESPACE, "contractorINN");
	public static final QName PROP_IS_DEFERRED_TASK = QName.createQName(SAMWF_NAMESPACE, "isDeferredTask");
	public static final QName PROP_REJECTION_REGISTRATION_DATE = QName.createQName(SAMWF_NAMESPACE, "rejectionSignTaskRegistrationDate");
	public static final QName PROP_REJECTION_ID_EDI = QName.createQName(SAMWF_NAMESPACE, "rejectionSignTaskIdEdi");
	public static final QName PROP_REJECTION_SIGNED_CONTRACTOR = QName.createQName(SAMWF_NAMESPACE, "rejectionSignTaskSignedContractor");
	public static final QName PROP_REJECTION_INCOME_PACKAGE_TASK_ID = QName.createQName(SAMWF_NAMESPACE, "rejectionSignTaskIncomePackageTaskId");
	public static final QName PROP_REJECTION_COMMENT = QName.createQName(SAMWF_NAMESPACE, "rejectionSignTaskComment");
	public static final QName PROP_ABSTRACT_COUNTERPARTY = QName.createQName(SAMWF_NAMESPACE, "abstractCounterparty");
	public static final QName PROP_CLIENT_BOX_ID = QName.createQName(SAMWF_NAMESPACE, "clientBoxId");

	//Associations
	public static final QName ASSOC_ATTACHMENTS = QName.createQName(SAMWF_NAMESPACE, "attachments");
	public static final QName ASSOC_CONTRACTOR = QName.createQName(SAMWF_NAMESPACE, "contractor");
	public static final QName ASSOC_REJECTION_DOCUMENTS = QName.createQName(SAMWF_NAMESPACE, "rejectionSignTaskDocuments");
	public static final QName ASSOC_REJECTION_CONTRACTOR = QName.createQName(SAMWF_NAMESPACE, "rejectionSignTaskContractor");

	//Statuses
	public static final String PROCESSING_STATUS_PROCESSED = "Processed";
	public static final String PROCESSING_STATUS_PARTIALLY_PROCESSED = "PartiallyProcessed";
	public static final String PROCESSING_STATUS_NOT_PROCESSED = "NotProcessed";
}
