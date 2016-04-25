/*
 * Copyright (C) 2008-2016 Citeck LLC.
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
 * @author Roman.Makarskiy on 04.04.2016.
 */
public final class PaymentsModel {
    public static final String PAYMENTS_NAMESPACE = "http://www.citeck.ru/model/payments/1.0";

    public static final QName TYPE = QName.createQName(PAYMENTS_NAMESPACE, "payment");

    public static final QName PROP_PAYMENT_AMOUNT = QName.createQName(PAYMENTS_NAMESPACE, "paymentAmount");
    public static final QName PROP_PAYMENT_AMOUNT_IN_WORDS = QName.createQName(PAYMENTS_NAMESPACE, "paymentAmountInWords");

    public static final QName ASSOC_PAYMENT_CURRENCY = QName.createQName(PAYMENTS_NAMESPACE, "currency");
}
