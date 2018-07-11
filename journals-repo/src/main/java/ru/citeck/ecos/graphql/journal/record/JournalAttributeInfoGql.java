package ru.citeck.ecos.graphql.journal.record;

import org.alfresco.service.namespace.QName;

import java.util.Collections;
import java.util.List;

public interface JournalAttributeInfoGql {

    String name();

    default List<String> getDefaultInnerAttributes() {
        return Collections.singletonList("str");
    }

    QName getDataType();
}
