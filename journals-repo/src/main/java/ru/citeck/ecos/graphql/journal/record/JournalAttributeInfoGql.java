package ru.citeck.ecos.graphql.journal.record;

import java.util.Collections;
import java.util.List;

public interface JournalAttributeInfoGql {

    String name();

    default List<String> getDefaultInnerAttributes() {
        return Collections.singletonList("data");
    }
}
