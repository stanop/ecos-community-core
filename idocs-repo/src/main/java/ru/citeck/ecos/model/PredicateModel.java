package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public final class PredicateModel {

	public static final String NAMESPACE = "http://www.citeck.ru/model/predicate/1.0";
	
	public static final QName TYPE_PREDICATE = QName.createQName(NAMESPACE, "predicate");
	
	public static final QName TYPE_CONDITION = QName.createQName(NAMESPACE, "condition");
	public static final QName ASSOC_ANTECEDENT = QName.createQName(NAMESPACE, "antecedent");
	public static final QName ASSOC_CONSEQUENT = QName.createQName(NAMESPACE, "consequent");
	
	public static final QName TYPE_KIND_PREDICATE = QName.createQName(NAMESPACE, "kindPredicate");
	public static final QName PROP_REQUIRED_TYPE = QName.createQName(NAMESPACE, "requiredType");
	public static final QName PROP_REQUIRED_KIND = QName.createQName(NAMESPACE, "requiredKind");

	public static final QName ASPECT_HAS_QUANTIFIER = QName.createQName(NAMESPACE, "hasQuantifier");
	public static final QName PROP_QUANTIFIER = QName.createQName(NAMESPACE, "quantifier");

}
