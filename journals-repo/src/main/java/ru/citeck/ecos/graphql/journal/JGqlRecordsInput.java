package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class JGqlRecordsInput extends HashMap<String, Object> {

    private static final String PROP_REMOTE_REFS = "remoteRefs";

    @GraphQLField
    private List<String> remoteRefs;


    public JGqlRecordsInput(
            @GraphQLName(PROP_REMOTE_REFS) List<String> remoteRefs
    ) {
        super(1);
        setRemoteRefs(remoteRefs);
    }


    public List<String> getRemoteRefs() {
        return remoteRefs;
    }

    public void setRemoteRefs(List<String> remoteRefs) {
        this.remoteRefs = remoteRefs != null ? remoteRefs : Collections.emptyList();
        put(PROP_REMOTE_REFS, remoteRefs);
    }
}
