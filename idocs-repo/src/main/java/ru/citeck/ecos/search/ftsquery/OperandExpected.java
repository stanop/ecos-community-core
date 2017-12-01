package ru.citeck.ecos.search.ftsquery;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

public interface OperandExpected {

    OperatorExpected values(Map<QName, Serializable> values);
    OperatorExpected values(Map<QName, Serializable> values, BinOperator joinOperator);
    OperatorExpected values(Map<QName, Serializable> values, BinOperator joinOperator, boolean exact);
    OperatorExpected exact(QName field, Serializable value);
    OperatorExpected value(QName field, Serializable value);
    OperatorExpected value(QName field, Serializable value, boolean exact);

    OperatorExpected isSet(QName field);
    OperatorExpected isUnset(QName field);
    OperatorExpected isNull(QName field);
    OperatorExpected isNotNull(QName field);
    OperatorExpected empty(QName field);

    OperatorExpected parent(NodeRef parent);
    OperatorExpected type(QName typeName);

    OperandExpected not();
    OperandExpected start();

    OperandExpected transactional();

}
