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
 * @author: Alexander Nemerov
 * @date: 27.01.14
 */
public final class IdocsModel {

    // model
    public static final String IDOCS_MODEL_PREFIX = "idocs";

    // namespace
    public static final String IDOCS_NAMESPACE = "http://www.citeck.ru/model/content/idocs/1.0";

    // types
    public static final QName TYPE_DOC = QName.createQName(IDOCS_NAMESPACE, "doc");
    public static final QName TYPE_INTERNAL = QName.createQName(IDOCS_NAMESPACE, "internal");
    public static final QName TYPE_ATTORNEY = QName.createQName(IDOCS_NAMESPACE, "powerOfAttorney");
    public static final QName TYPE_LEGAL_ENTITY = QName.createQName(IDOCS_NAMESPACE, "legalEntity");

    // aspects
    public static final QName ASPECT_LIFECYCLE = QName.createQName(IDOCS_NAMESPACE, "lifeCycle");

    // properties
    public static final QName PROP_REGISTRATION_DATE = QName.createQName(IDOCS_NAMESPACE, "registrationDate");
    public static final QName PROP_REGISTRATION_NUMBER = QName.createQName(IDOCS_NAMESPACE, "registrationNumber");
    public static final QName PROP_DOCUMENT_STATUS = QName.createQName(IDOCS_NAMESPACE, "documentStatus");

    public static final QName PROP_CODE = QName.createQName(IDOCS_NAMESPACE, "code");
    public static final QName PROP_FULL_NAME = QName.createQName(IDOCS_NAMESPACE, "fullName");
    public static final QName PROP_LEGAL_ADDRESS = QName.createQName(IDOCS_NAMESPACE, "legalAddress");
    public static final QName PROP_PHONE_NUMBER = QName.createQName(IDOCS_NAMESPACE, "phoneNumber");
    public static final QName PROP_OKPO = QName.createQName(IDOCS_NAMESPACE, "okpo");
    public static final QName PROP_OGRN = QName.createQName(IDOCS_NAMESPACE, "ogrn");
    public static final QName PROP_INN = QName.createQName(IDOCS_NAMESPACE, "inn");
    public static final QName PROP_KPP = QName.createQName(IDOCS_NAMESPACE, "kpp");
    public static final QName PROP_SHORT_ORGANIZATION_NAME = QName.createQName(IDOCS_NAMESPACE, "shortOrganizationName");
    public static final QName PROP_COUNTRY_NAME = QName.createQName(IDOCS_NAMESPACE, "countryName");
    public static final QName PROP_POST_CODE = QName.createQName(IDOCS_NAMESPACE, "postCode");
    public static final QName PROP_REGION_NAME = QName.createQName(IDOCS_NAMESPACE, "regionName");
    public static final QName PROP_DISTRICT_NAME = QName.createQName(IDOCS_NAMESPACE, "districtName");
    public static final QName PROP_CITY_NAME = QName.createQName(IDOCS_NAMESPACE, "cityName");
    public static final QName PROP_STREET_NAME = QName.createQName(IDOCS_NAMESPACE, "streetName");
    public static final QName PROP_HOUSE = QName.createQName(IDOCS_NAMESPACE, "house");
    public static final QName PROP_ADDRESS_EXTRA_INFO = QName.createQName(IDOCS_NAMESPACE, "addressExtraInfo");
    public static final QName PROP_CURRENCY_CODE = QName.createQName(IDOCS_NAMESPACE, "currencyCode");

    //assocs
    public static final QName ASSOC_GENERAL_DIRECTOR = QName.createQName(IDOCS_NAMESPACE, "generalDirector");
    public static final QName ASSOC_ACCOUNTANT_GENERAL = QName.createQName(IDOCS_NAMESPACE, "accountantGeneral");

}
