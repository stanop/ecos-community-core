/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 *
 */
public final class SecurityModel {

    // model

    public static final String SECURITY_MODEL_PREFIX = "sam";

    // namespace
    public static final String SECURITY_NAMESPACE = "http://www.citeck.ru/model/sam/1.0";

    // types
    public static final QName TYPE_PACKAGE = QName.createQName(SECURITY_NAMESPACE, "package");
    public static final QName TYPE_INBOUND_PACKAGE = QName.createQName(SECURITY_NAMESPACE, "inboundPackage");
    public static final QName TYPE_OUTBOUND_PACKAGE = QName.createQName(SECURITY_NAMESPACE, "outboundPackage");
    public static final QName TYPE_REJECTION_COMMENT = QName.createQName(SECURITY_NAMESPACE, "rejectionComment");
    public static final QName TYPE_REVOCATION_XML = QName.createQName(SECURITY_NAMESPACE, "revocationXml");
    public static final QName TYPE_SIGNABLE_XML = QName.createQName(SECURITY_NAMESPACE, "signableXml");
    public static final QName TYPE_SIGN = QName.createQName(SECURITY_NAMESPACE, "sign");

    // aspects
    public static final QName ASPECT_SIGNABLE = QName.createQName(SECURITY_NAMESPACE, "signable");
    public static final QName ASPECT_HAS_CONTRACTOR_SIGN = QName.createQName(SECURITY_NAMESPACE, "hasContractorSign");
    public static final QName ASPECT_HAS_PACKAGE_ATTACHMENTS = QName.createQName(SECURITY_NAMESPACE, "hasPackageAttachments");
    public static final QName ASPECT_HAS_ENTITY_ID = QName.createQName(SECURITY_NAMESPACE, "hasEntityId");
    public static final QName ASPECT_HAS_PACKAGE_STATUS_COPY = QName.createQName(SECURITY_NAMESPACE, "hasPackageStatusCopy");
    public static final QName ASPECT_HAS_SHOULD_BE_SIGNED = QName.createQName(SECURITY_NAMESPACE, "hasShouldBeSigned");
    public static final QName ASPECT_IS_REJECTABLE = QName.createQName(SECURITY_NAMESPACE, "isRejectable");
    public static final QName ASPECT_HAS_CONTRACTOR_SIGN_VALID = QName.createQName(SECURITY_NAMESPACE, "hasContractorSignValid");
    public static final QName ASPECT_HAS_ATTACHMENT_TYPE = QName.createQName(SECURITY_NAMESPACE, "hasAttachmentType");
    public static final QName ASPECT_HAS_ATTACHMENT_VERSION = QName.createQName(SECURITY_NAMESPACE, "hasAttachmentVersion");
    public static final QName ASPECT_HAS_CONTENT_FROM_INBOUND_PACKAGE = QName.createQName(SECURITY_NAMESPACE, "hasContentFromInboundPackage");
    public static final QName ASPECT_HAS_CLIENT_BOX_ID = QName.createQName(SECURITY_NAMESPACE, "hasClientBoxId");

    // properties
    public static final QName PROP_SIGNATURE_VALUE = QName.createQName(SECURITY_NAMESPACE, "signValue");
    public static final QName PROP_SIGNER = QName.createQName(SECURITY_NAMESPACE, "signer");
    public static final QName PROP_SIGN_DATE = QName.createQName(SECURITY_NAMESPACE, "signDate");
    public static final QName PROP_PACKAGE_ID = QName.createQName(SECURITY_NAMESPACE, "packageId");
    public static final QName PROP_MESSAGE_ID = QName.createQName(SECURITY_NAMESPACE, "messageId");
    public static final QName PROP_PACKAGE_STATUS = QName.createQName(SECURITY_NAMESPACE, "packageStatus");
    public static final QName PROP_PACKAGE_ATMNT_STATUS = QName.createQName(SECURITY_NAMESPACE, "packageAttachmentStatus");
    public static final QName PROP_PACKAGE_ERROR_CODE = QName.createQName(SECURITY_NAMESPACE, "packageErrorCode");
    public static final QName PROP_ATTACHMENT_TYPE = QName.createQName(SECURITY_NAMESPACE, "attachmentType");
    public static final QName PROP_ATTACHMENT_VERSION = QName.createQName(SECURITY_NAMESPACE, "attachmentVersion");
    public static final QName PROP_ENTITY_ID = QName.createQName(SECURITY_NAMESPACE, "entityId");
    public static final QName PROP_COMMENT = QName.createQName(SECURITY_NAMESPACE, "comment");
    public static final QName PROP_CONTRACTOR_SIGN_VALUE = QName.createQName(SECURITY_NAMESPACE, "contractorSignValue");
    public static final QName PROP_PACKAGE_STATUS_COPY = QName.createQName(SECURITY_NAMESPACE, "packageStatusCopy");
    public static final QName PROP_SHOULD_BE_SIGNED = QName.createQName(SECURITY_NAMESPACE, "shouldBeSigned");
    public static final QName PROP_COUNTERPARTY_BOX_ID = QName.createQName(SECURITY_NAMESPACE, "counterPartyBoxId");
    public static final QName PROP_IS_SIGN_VALID = QName.createQName(SECURITY_NAMESPACE, "isSignValid");
    public static final QName PROP_SIGNER_FULL_NAME = QName.createQName(SECURITY_NAMESPACE, "signerFullName");
    public static final QName PROP_SIGNER_EMAIL = QName.createQName(SECURITY_NAMESPACE, "signerEmail");
    public static final QName PROP_SIGNER_POSITION = QName.createQName(SECURITY_NAMESPACE, "signerPosition");
    public static final QName PROP_SIGNER_COMPANY = QName.createQName(SECURITY_NAMESPACE, "signerCompany");
    public static final QName PROP_CERTIFICATE_SN = QName.createQName(SECURITY_NAMESPACE, "certificateSerialNumber");
    public static final QName PROP_CERTIFICATE_ISSUER_NAME = QName.createQName(SECURITY_NAMESPACE, "certificateIssuerName");
    public static final QName PROP_CERTIFICATE_START_DATE = QName.createQName(SECURITY_NAMESPACE, "certificateStartDate");
    public static final QName PROP_CERTIFICATE_END_DATE = QName.createQName(SECURITY_NAMESPACE, "certificateEndDate");
    public static final QName CERTIFICATE_ISSUER_ORGANIZATION = QName.createQName(SECURITY_NAMESPACE, "certificateIssuerOrganization");
    public static final QName PROP_CONTRACTOR_SIGN_DATE = QName.createQName(SECURITY_NAMESPACE, "contractorSignDate");
    public static final QName PROP_IS_CONTRACTOR_SIGN_VALID = QName.createQName(SECURITY_NAMESPACE, "isContractorSignValid");
    public static final QName PROP_PERCENT_COMPLETE = QName.createQName(SECURITY_NAMESPACE, "percentComplete");
    public static final QName PROP_IS_CERTIFICATE_VALID = QName.createQName(SECURITY_NAMESPACE, "isCertificateValid");
    public static final QName PROP_CLIENT_BOX_ID = QName.createQName(SECURITY_NAMESPACE, "clientBoxId");
    public static final QName PROP_SIGNED_IN_EDMS = QName.createQName(SECURITY_NAMESPACE, "signedInEdms");

    //assocs
    public static final QName ASSOC_PACKAGE_ATTACHMENTS = QName.createQName(SECURITY_NAMESPACE, "packageAttachments");
    public static final QName ASSOC_PACKAGE_DOCUMENT_LINK = QName.createQName(SECURITY_NAMESPACE, "packageDocumentLink");
    public static final QName ASSOC_REJECTION_LINK = QName.createQName(SECURITY_NAMESPACE, "rejectionCommentLink");
    public static final QName ASSOC_REVOCATION_LINK = QName.createQName(SECURITY_NAMESPACE, "revocationXmlLink");
    public static final QName ASSOC_INVOICE_CORRECTION_REQUEST_XML_LINK = QName.createQName(SECURITY_NAMESPACE, "invoiceCorrectionRequestXmlLink");
    public static final QName ASSOC_BUYER_TITLE_XML_LINK = QName.createQName(SECURITY_NAMESPACE, "buyerTitleXmlLink");
    public static final QName ASSOC_CONTRACTOR_SIGN_LINK = QName.createQName(SECURITY_NAMESPACE, "contractorSignLink");
    public static final QName ASSOC_SIGN_LINK = QName.createQName(SECURITY_NAMESPACE, "signLink");
    public static final QName ASSOC_SIGN_COPY = QName.createQName(SECURITY_NAMESPACE, "signCopy");
    public static final QName ASSOC_CONTENT_FROM_INBOUND_PACKAGE = QName.createQName(SECURITY_NAMESPACE, "contentFromInboundPackage");

    //package statuses
    public static final String PKG_STATUS_SENT = "SENT";
    public static final String PKG_STATUS_SIGNED = "SIGNED";
    public static final String PKG_STATUS_REJECTED = "REJECTED";
    public static final String PKG_STATUS_DELIVERED = "DELIVERED";
    public static final String PKG_STATUS_DELIVERY_FAILED = "DELIVERY_FAILED";
    public static final String PKG_STATUS_RECEIVED = "RECEIVED";

    //package attachment statuses
    public static final String PKG_ATMNT_STATUS_SENT = "SENT";
    public static final String PKG_ATMNT_STATUS_SIGNED = "SIGNED";
    public static final String PKG_ATMNT_STATUS_REJECTED = "REJECTED";
    public static final String PKG_ATMNT_STATUS_REJECTION_SENT = "REJECTION_SENT";
    public static final String PKG_ATMNT_STATUS_REQUESTS_MY_REVOCATION = "REQUESTS_MY_REVOCATION";
    public static final String PKG_ATMNT_STATUS_REVOCATION_ACCEPTED = "REVOCATION_ACCEPTED";
    public static final String PKG_ATMNT_STATUS_REVOCATION_REJECTED = "REVOCATION_REJECTED";
    public static final String PKG_ATMNT_STATUS_REVOCATION_IS_REQUESTED_BY_ME = "REVOCATION_IS_REQUESTED_BY_ME";
    public static final String PKG_ATMNT_STATUS_DELIVERED = "DELIVERED";
    public static final String PKG_ATMNT_STATUS_DELIVERY_FAILED = "DELIVERY_FAILED";
    public static final String PKG_ATMNT_STATUS_RECEIVED = "RECEIVED";
    public static final String PKG_ATMNT_STATUS_CORRECTION_REQUESTED = "CORRECTION_REQUESTED";
    public static final String PKG_ATMNT_STATUS_REVISIONED = "REVISIONED";
    public static final String PKG_ATMNT_STATUS_CORRECTED = "CORRECTED";
    public static final String PKG_ATMNT_STATUS_REVISION_CORRECTED = "REVISION_CORRECTED";
    public static final String PKG_ATMNT_STATUS_BUYER_TITLE_SIGNED = "BUYER_TITLE_SIGNED";
    public static final String PKG_ATMNT_STATUS_SIGN_SENT = "SIGN_SENT";
}