package ru.citeck.ecos.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchCriteriaSettingsRegistry {
    private static final String PREDICATE_OR = SearchPredicate.QUERY_OR.getValue();

    private Map<String, String> journalStaticQuery = new ConcurrentHashMap<>();
    private Map<String, String> journalNodeType = new ConcurrentHashMap<>();
    private Map<String, List<String>> journalIdFieldNameCache = new ConcurrentHashMap<>();
    private Map<String, List<String>> journalSubQueryOr = new ConcurrentHashMap<>();
    private static AtomicInteger fieldIndex = new AtomicInteger(1);

    public void registerJournalNodeType(String journalId, String nodeType) {
        journalNodeType.put(journalId, nodeType);
    }

    public String getNodeType(String journalId) {
        return journalNodeType.get(journalId);
    }

    public void registerJournalStaticQuery(String journalId, String subQuery) {
        journalStaticQuery.put(journalId, subQuery);
    }

    public String getStaticQuery(String journalId) {
        return journalStaticQuery.get(journalId);
    }

    public void cleanFieldNameCache(String journalId) {
        List<String> generatedFieldList = journalIdFieldNameCache.get(journalId);
        if (generatedFieldList != null) {
            for (String fieldName : generatedFieldList) {
                journalSubQueryOr.remove(fieldName);
            }
            generatedFieldList.clear();
        }
    }

    public String registerJournalSubQueryOr(String journalId, List<String> json) {
        String generatedFieldName = PREDICATE_OR + fieldIndex.getAndIncrement();
        List<String> generatedFieldList = journalIdFieldNameCache.computeIfAbsent(journalId,
                k -> Collections.synchronizedList(new ArrayList<>()));
        generatedFieldList.add(generatedFieldName);
        journalSubQueryOr.put(generatedFieldName, Collections.unmodifiableList(json));
        return generatedFieldName;
    }

    public List<String> getSubQueryOr(String generatedFieldName) {
        return journalSubQueryOr.get(generatedFieldName);
    }
}
