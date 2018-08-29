package ru.citeck.ecos.history.filter;

import java.util.List;
import java.util.Map;

public interface Criteria {
    List<Map> meetCriteria(List<Map> historyRecords);
}
