package ru.citeck.ecos.search;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class SearchCriteriaSerializer extends StdSerializer<SearchCriteria> {

    public SearchCriteriaSerializer() {
        super(SearchCriteria.class);
    }

    @Override
    public void serialize(SearchCriteria value,
                          JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        String fieldPrefix = SearchCriteriaParser.FIELD_KEY + SearchCriteriaParser.SEPARATOR;
        String predicatePrefix = SearchCriteriaParser.PREDICATE_KEY + SearchCriteriaParser.SEPARATOR;
        String valuePrefix = SearchCriteriaParser.VALUE_KEY + SearchCriteriaParser.SEPARATOR;

        int idx = 0;

        for (CriteriaTriplet triplet : value.getTriplets()) {
            jgen.writeStringField(fieldPrefix + idx, triplet.getField());
            jgen.writeStringField(predicatePrefix + idx, triplet.getPredicate());
            jgen.writeStringField(valuePrefix + idx, triplet.getValue());
            idx++;
        }

        jgen.writeEndObject();
    }
}
