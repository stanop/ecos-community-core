package ru.citeck.ecos.graphql.journal.record;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.records.AttributeInfo;

import java.util.Collections;
import java.util.List;

public interface JGqlAttributeInfo extends AttributeInfo {

    String name();

    default List<String> getDefaultInnerAttributes() {
        return Collections.singletonList("str");
    }

    QName getDataType();
}
