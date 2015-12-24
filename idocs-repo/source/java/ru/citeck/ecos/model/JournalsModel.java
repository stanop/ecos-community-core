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

public final class JournalsModel {

    // model
    public static final String JOURNAL_MODEL_PREFIX = "journal";

    // namespace
    public static final String JOURNAL_NAMESPACE = "http://www.citeck.ru/model/journals/1.0";

    // types
//    public static final QName TYPE_JOURNAL_CONFIG = QName.createQName(JOURNAL_NAMESPACE, "journalConfig");
    public static final QName TYPE_BASE = QName.createQName(JOURNAL_NAMESPACE, "base");
    public static final QName TYPE_JOURNAL = QName.createQName(JOURNAL_NAMESPACE, "journal");
    public static final QName TYPE_CRITERION = QName.createQName(JOURNAL_NAMESPACE, "criterion");
    public static final QName TYPE_FILTER = QName.createQName(JOURNAL_NAMESPACE, "filter");
    public static final QName TYPE_SETTINGS = QName.createQName(JOURNAL_NAMESPACE, "settings");
    public static final QName TYPE_JOURNALS_LIST = QName.createQName(JOURNAL_NAMESPACE, "journalsList");
    public static final QName TYPE_CREATE_VARIANT= QName.createQName(JOURNAL_NAMESPACE, "createVariant");

    // aspects
    public static final QName ASPECT_DEFAULT = QName.createQName(JOURNAL_NAMESPACE, "default");
    public static final QName ASPECT_HAS_CRITERIA = QName.createQName(JOURNAL_NAMESPACE, "hasCriteria");
    public static final QName ASPECT_JOURNAL_TYPE = QName.createQName(JOURNAL_NAMESPACE, "journalType");
    public static final QName ASPECT_JOURNAL_TYPES = QName.createQName(JOURNAL_NAMESPACE, "journalTypes");

    // properties
    public static final QName PROP_PREDICATE = QName.createQName(JOURNAL_NAMESPACE, "predicate");
    public static final QName PROP_CRITERION_VALUE = QName.createQName(JOURNAL_NAMESPACE, "criterionValue");
    public static final QName PROP_TYPE = QName.createQName(JOURNAL_NAMESPACE, "type");
    public static final QName PROP_FORM_ID = QName.createQName(JOURNAL_NAMESPACE, "formId");
    public static final QName PROP_JOURNAL_TYPE = QName.createQName(JOURNAL_NAMESPACE, "journalType");
    public static final QName PROP_FIELD_QNAME = QName.createQName(JOURNAL_NAMESPACE, "fieldQName");
    public static final QName PROP_IS_DEFAULT = QName.createQName(JOURNAL_NAMESPACE, "isDefault");


    //associations
    public static final QName ASSOC_JOURNALS = QName.createQName(JOURNAL_NAMESPACE, "journals");
    public static final QName ASSOC_SEARCH_CRITERIA = QName.createQName(JOURNAL_NAMESPACE, "searchCriteria");
    public static final QName ASSOC_CREATE_VARIANTS = QName.createQName(JOURNAL_NAMESPACE, "createVariants");
    public static final QName ASSOC_DESTINATION = QName.createQName(JOURNAL_NAMESPACE, "destination");









}
