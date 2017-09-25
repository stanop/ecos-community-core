package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public final class CriterionModel {

	public static final String NAMESPACE = "http://www.citeck.ru/model/criterion/1.0";
	public static final String PREDICATE_NAMESPACE = "http://www.citeck.ru/model/predicate/1.0";

	public static final QName TYPE_CRITERION = QName.createQName(NAMESPACE, "criterion");
	public static final QName PROP_CRITERION_ATTRIBUTE = QName.createQName(NAMESPACE, "attribute");
	public static final QName PROP_CRITERION_PREDICATE = QName.createQName(NAMESPACE, "predicate");
	public static final QName PROP_CRITERION_VALUE = QName.createQName(NAMESPACE, "value");

}
