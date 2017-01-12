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

    // aspects
    public static final QName ASPECT_SIGNABLE = QName.createQName(SECURITY_NAMESPACE, "signable");

    // properties
    public static final QName PROP_SIGNATURE_VALUE = QName.createQName(SECURITY_NAMESPACE, "signValue");
    public static final QName PROP_SIGNER = QName.createQName(SECURITY_NAMESPACE, "signer");

}
