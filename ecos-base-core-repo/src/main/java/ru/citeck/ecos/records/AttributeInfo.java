package ru.citeck.ecos.records;

import org.alfresco.service.namespace.QName;

import java.util.Collections;
import java.util.List;

public interface AttributeInfo {

    String name();

    default List<String> getDefaultInnerAttributes() {
        return Collections.singletonList("str");
    }

    QName getDataType();
}
