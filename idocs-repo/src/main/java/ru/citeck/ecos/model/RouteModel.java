package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface RouteModel {

    String NAMESPACE = "http://www.citeck.ru/model/route/1.0";

	QName TYPE_ROUTE = QName.createQName(NAMESPACE, "route");
	QName TYPE_STAGE = QName.createQName(NAMESPACE, "stage");
	QName TYPE_PARTICIPANT = QName.createQName(NAMESPACE, "participant");

	QName PROP_PRECEDENCE = QName.createQName(NAMESPACE, "precedence");

	QName ASSOC_PARTICIPANTS = QName.createQName(NAMESPACE, "participants");
	QName ASSOC_STAGES = QName.createQName(NAMESPACE, "stages");
	QName ASSOC_AUTHORITY = QName.createQName(NAMESPACE, "authority");
	QName ASSOC_ROUTE = QName.createQName(NAMESPACE, "routeAssoc");

	QName ASPECT_HAS_ROUTE = QName.createQName(NAMESPACE, "hasRoute");
	QName ASPECT_HAS_PRECEDENCE = QName.createQName(NAMESPACE, "hasPrecedence");

}
