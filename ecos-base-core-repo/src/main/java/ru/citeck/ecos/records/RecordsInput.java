package ru.citeck.ecos.records;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RecordsInput extends HashMap<String, Object> {

    private static final String PROP_REMOTE_REFS = "refs";

    @GraphQLField
    private List<String> refs;

    public RecordsInput(@GraphQLName(PROP_REMOTE_REFS) List<String> refs) {
        super(1);
        setRefs(refs);
    }

    public List<String> getRefs() {
        return refs;
    }

    public void setRefs(List<String> refs) {
        this.refs = refs != null ? refs : Collections.emptyList();
        put(PROP_REMOTE_REFS, refs);
    }
}
