package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * @author: Sergey Tiunov
 * @date: 01.04.2015
 */
public final class LettersModel {

    public static final String NAMESPACE = "http://www.citeck.ru/model/letters/1.0";

    public static final QName TYPE_INCOME = QName.createQName(NAMESPACE, "income");
    public static final QName TYPE_OUTCOME = QName.createQName(NAMESPACE, "outcome");

}
