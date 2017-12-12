package ru.citeck.ecos.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchCriteriaSettingsRegistry {
    private Map<String, String> journalStaticQuery = new HashMap<>();
    private Map<String, String> journalNodeType = new HashMap<>();
    private Map<String, List<String>> journalSubQueryOr = new HashMap<>();
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

    public String registerJournalSubQueryOr(String fieldName, List<String> json) {
        String fieldUniqueName = fieldName + fieldIndex.getAndIncrement();
        journalSubQueryOr.put(fieldUniqueName, json);

        return fieldUniqueName;
    }

    public List<String> getSubQueryOr(String fieldName) {
        return journalSubQueryOr.get(fieldName);
    }
}
