package ru.citeck.ecos.graphql.journal;

import graphql.annotations.annotationTypes.GraphQLField;

public class JournalGqlPageInfo {

    private String endCursor;
    private boolean hasNextPage;

    @GraphQLField
    public String endCursor() {
        return endCursor;
    }

    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    @GraphQLField
    public boolean hasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }
}
