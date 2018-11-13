package ru.citeck.ecos.graphql.journal.response.converter;

import graphql.ExecutionResult;
import ru.citeck.ecos.graphql.journal.response.JournalData;

import java.util.Map;

public interface ResponseConverter {

    JournalData convert(ExecutionResult source, Map<String, Object> additionalData);

}
