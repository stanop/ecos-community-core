package ru.citeck.ecos.graphql.journal.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.search.CriteriaTriplet;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchCriteriaParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: alexander.nemerov
 * Date: 27.04.2018.
 */
public class FiasAddressDBDataSource extends DbJournalDataSource {

    @Autowired
    private SearchCriteriaParser criteriaParser;

    @Override
    protected String sqlFromTemplate(String sqlQueryTemplate, String query, String language) {

        SearchCriteria criteria = criteriaParser.parse(query);
        List<CriteriaTriplet> triplets = criteria.getTriplets();
        Map<String, CriteriaTriplet> tripletMap = new HashMap<>();
        for (CriteriaTriplet triplet : triplets) {
            tripletMap.put(triplet.getField(), triplet);
        }
        String formalName = tripletMap.containsKey("cm:formal_name") ? "" + tripletMap.get("cm:formal_name").getValue() : "";
        String sqlQuery = sqlQueryTemplate.replace(":formalName", formalName);

        String additionFilters = "";
        if (tripletMap.containsKey("cm:place_code")) {
            additionFilters = " and place_code=" + tripletMap.get("cm:place_code").getValue();
        }
        if (tripletMap.containsKey("cm:city_code")) {
            additionFilters += " and city_code=" + tripletMap.get("cm:city_code").getValue();
        }
        if (tripletMap.containsKey("cm:area_code")) {
            additionFilters += " and area_code=" + tripletMap.get("cm:area_code").getValue();
        }
        if (tripletMap.containsKey("cm:region_code")) {
            additionFilters += " and region_code=" + tripletMap.get("cm:region_code").getValue();
        }

        sqlQuery = sqlQuery.replace(":additionFilters", additionFilters);

        return sqlQuery;
    }

}
