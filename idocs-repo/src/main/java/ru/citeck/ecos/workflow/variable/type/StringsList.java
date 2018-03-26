package ru.citeck.ecos.workflow.variable.type;

import java.util.ArrayList;
import java.util.Collection;

public class StringsList extends ArrayList<String> implements EcosPojoType {

    public StringsList() {
    }

    public StringsList(Collection<String> other) {
        super(other);
    }
}
