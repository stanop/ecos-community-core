package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface EcosModel {

    // model
    String ECOS_MODEL_PREFIX = "ecos";

    //Namespaces
    public static final String ECOS_NAMESPACE = "http://www.citeck.ru/model/content/ecos/1.0";

    //Types
    public static final QName TYPE_DOCUMENT = QName.createQName(ECOS_NAMESPACE, "document");
    public static final QName TYPE_CASE = QName.createQName(ECOS_NAMESPACE, "case");

    //Aspects
    public static final QName ASPECT_HAS_RESPONSIBLE = QName.createQName(ECOS_NAMESPACE, "hasResponsible");
    public static final QName ASPECT_DOCUMENT_TYPE = QName.createQName(ECOS_NAMESPACE, "documentType");
    public static final QName ASPECT_HAS_NUMBER_AND_DATE = QName.createQName(ECOS_NAMESPACE, "hasNumberAndDate");
    public static final QName ASPECT_HAS_AMOUNT = QName.createQName(ECOS_NAMESPACE, "hasAmount");
    public static final QName ASPECT_HAS_VAT = QName.createQName(ECOS_NAMESPACE, "hasVAT");
    public static final QName ASPECT_HAS_ADDITIONAL_PERSON_PROPERTIES = QName.createQName(ECOS_NAMESPACE, "hasAdditionalPersonProperties");

    //Properties
    public static final QName PROP_DOCUMENT_NUMBER = QName.createQName(ECOS_NAMESPACE, "documentNumber");
    public static final QName PROP_DOCUMENT_DATE = QName.createQName(ECOS_NAMESPACE, "documentDate");
    public static final QName PROP_DOCUMENT_AMOUNT = QName.createQName(ECOS_NAMESPACE, "documentAmount");
    public static final QName PROP__VAT = QName.createQName(ECOS_NAMESPACE, "VAT");
    public static final QName PROP_PHOTO = QName.createQName(ECOS_NAMESPACE, "photo");
    public static final QName PROP_BIRTH_DATE = QName.createQName(ECOS_NAMESPACE, "birthDate");
    public static final QName PROP_BIRTH_MONTH_DAY = QName.createQName(ECOS_NAMESPACE, "birthMonthDay");
    public static final QName PROP_CITY = QName.createQName(ECOS_NAMESPACE, "city");
    public static final QName PROP_SEX = QName.createQName(ECOS_NAMESPACE, "sex");
    public static final QName PROP_PHONE_WORKING = QName.createQName(ECOS_NAMESPACE, "phoneWorking");
    public static final QName PROP_PHONE_INTERNAL = QName.createQName(ECOS_NAMESPACE, "phoneInternal");
    public static final QName PROP_OLD_PASS = QName.createQName(ECOS_NAMESPACE, "oldPass");
    public static final QName PROP_PASS = QName.createQName(ECOS_NAMESPACE, "pass");
    public static final QName PROP_PASS_VERIFY = QName.createQName(ECOS_NAMESPACE, "passVerify");
    public static final QName PROP_IS_PERSON_DISABLED = QName.createQName(ECOS_NAMESPACE, "isPersonDisabled");

    //Associations
    public static final QName ASSOC_RESPONSIBLE = QName.createQName(ECOS_NAMESPACE, "responsible");

}
