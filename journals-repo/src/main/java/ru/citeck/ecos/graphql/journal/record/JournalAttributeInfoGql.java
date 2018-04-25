package ru.citeck.ecos.graphql.journal.record;

import java.util.Collections;
import java.util.List;

public interface JournalAttributeInfoGql {

    default List<String> getDefaultChildAttributes() {
        return Collections.singletonList("disp");
    }
}
