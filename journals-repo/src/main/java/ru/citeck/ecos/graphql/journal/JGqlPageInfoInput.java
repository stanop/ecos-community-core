package ru.citeck.ecos.graphql.journal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@JsonDeserialize(using = JGqlPageInfoInput.JsonDeserializer.class)
public class JGqlPageInfoInput extends HashMap<String, Object> {

    public static final JGqlPageInfoInput DEFAULT = new JGqlPageInfoInput(null, 100,
                                                                          Collections.emptyList(), 0);

    private static final String PROP_SKIP_COUNT = "skipCount";
    private static final String PROP_AFTER_ID = "afterId";
    private static final String PROP_MAX_ITEMS = "maxItems";
    private static final String PROP_SORT_BY = "sortBy";

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    @GraphQLField
    private int skipCount;
    @GraphQLField
    private int maxItems;
    @GraphQLField
    private List<JGqlSortBy> sortBy;
    @GraphQLField
    private String afterId;

    public JGqlPageInfoInput(
            @GraphQLName(PROP_AFTER_ID) String afterId,
            @GraphQLName(PROP_MAX_ITEMS) Integer maxItems,
            @GraphQLName(PROP_SORT_BY) List<JGqlSortBy> sortBy,
            @GraphQLName(PROP_SKIP_COUNT) Integer skipCount
    ) {
        super(4);

        setAfterId(afterId);
        setMaxItems(maxItems);
        setSortBy(sortBy);
        setSkipCount(skipCount);
    }

    public int getSkipCount() {
        return skipCount;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public List<JGqlSortBy> getSortBy() {
        return sortBy;
    }

    public String getAfterId() {
        return afterId;
    }

    public void setAfterId(String afterId) {
        this.afterId = afterId;
        put(PROP_AFTER_ID, this.afterId);
    }

    public void setSkipCount(Integer skipCount) {
        this.skipCount = skipCount != null ? skipCount : 0;
        put(PROP_SKIP_COUNT, this.skipCount);
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems != null ? maxItems : DEFAULT_PAGE_SIZE;
        put(PROP_MAX_ITEMS, this.maxItems);
    }

    public void setSortBy(List<JGqlSortBy> sortBy) {
        this.sortBy = sortBy != null ? sortBy : Collections.emptyList();
        put(PROP_SORT_BY, this.sortBy);
    }

    public static class JsonDeserializer extends StdDeserializer<JGqlPageInfoInput> {

        public JsonDeserializer() {
            super(JGqlPageInfoInput.class);
        }

        @Override
        public JGqlPageInfoInput deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

            JsonNode node = jp.getCodec().readTree(jp);

            Integer maxItems = asInt(node.get(PROP_MAX_ITEMS));
            Integer skipCount = asInt(node.get(PROP_SKIP_COUNT));
            String afterId = asText(node.get(PROP_AFTER_ID));

            JsonNode sortByNode = node.get(PROP_SORT_BY);
            List<JGqlSortBy> sortBy = null;

            if (sortByNode != null) {
                
                sortBy = new ArrayList<>();

                for (JsonNode sortNode : sortByNode) {

                    String attribute = asText(sortNode.get(JGqlSortBy.PROP_ATTRIBUTE));
                    String order = asText(sortNode.get(JGqlSortBy.PROP_ORDER));

                    sortBy.add(new JGqlSortBy(attribute, order));
                }
            }

            return new JGqlPageInfoInput(afterId, maxItems, sortBy, skipCount);
        }

        private String asText(JsonNode node) {
            return node != null ? node.asText() : null;
        }

        private Integer asInt(JsonNode node) {
            return node != null ? node.asInt(0) : null;
        }
    }
}
