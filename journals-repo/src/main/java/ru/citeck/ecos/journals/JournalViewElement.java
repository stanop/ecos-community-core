package ru.citeck.ecos.journals;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.citeck.ecos.journals.xml.Option;
import ru.citeck.ecos.journals.xml.ViewElement;
import ru.citeck.ecos.search.SearchCriteriaSettingsRegistry;
import ru.citeck.ecos.search.SearchPredicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalViewElement {
    private static final Logger logger = Logger.getLogger(JournalType.class);
    private static final String PREDICATE_OR = SearchPredicate.QUERY_OR.getValue();
    private static final String SEARCH_CRITERIA = "searchCriteria";
    private static final String PREDICATE = "predicate";
    private static final String ATTRIBUTE = "attribute";


    private String template;
    private Map<String, String> params = new HashMap<>();
    private String journalId;
    private SearchCriteriaSettingsRegistry registry;

    public JournalViewElement(ViewElement element, String journalId,
                              SearchCriteriaSettingsRegistry searchCriteriaSettingsRegistry) {
        this.journalId = journalId;
        this.registry = searchCriteriaSettingsRegistry;
        set(element);
    }

    public void set(ViewElement element) {
        if (element != null) {
            if (element.getTemplate() != null) {
                template = element.getTemplate();
            }
            List<Option> xmlParams = element.getParam();
            if (xmlParams != null) {
                xmlParams = transformSearchCriteria(xmlParams);
                for (Option o : xmlParams) {
                    params.put(o.getName(), o.getValue());
                }
            }
        }
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, String> getParams() {
        return params;
    }

    private List<Option> transformSearchCriteria(List<Option> xmlParams) {
        for (Option o : xmlParams) {
            if (o.getName().equals(SEARCH_CRITERIA)) {
                try {
                    JSONArray jsonArray = new JSONArray(o.getValue());

                    List<String> criteriaOr = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject criterion = (JSONObject) jsonArray.get(i);
                        if (PREDICATE_OR.equals(criterion.get(PREDICATE))) {
                            if (criterion.get(ATTRIBUTE) != null) {
                                criteriaOr.add(criterion.getString(ATTRIBUTE));
                            }
                        }
                    }
                    if (!criteriaOr.isEmpty()) {
                        JSONArray jsonResult = new JSONArray();
                        Map<String, String> jsonMap = new HashMap<>();
                        String fieldNameUniq = registry.registerJournalSubQueryOr(journalId, criteriaOr);
                        jsonMap.put(PREDICATE, PREDICATE_OR);
                        jsonMap.put(ATTRIBUTE, fieldNameUniq);
                        jsonResult.put(jsonMap);
                        for (int j = 0; j < jsonArray.length(); j++) {
                            JSONObject criterion = (JSONObject) jsonArray.get(j);
                            if (!PREDICATE_OR.equals(criterion.get(PREDICATE))) {
                                jsonResult.put(criterion);
                            }
                        }
                        o.setValue(jsonResult.toString().replace("\"", "'"));
                    }
                } catch (Exception e) {
                    logger.warn("Unable parse json: '" + o.getValue() + "'");
                }
            }
        }
        return xmlParams;
    }
}
