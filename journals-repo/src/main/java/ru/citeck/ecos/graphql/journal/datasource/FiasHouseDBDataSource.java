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
public class FiasHouseDBDataSource extends DbJournalDataSource{

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
        String houseNum = tripletMap.containsKey("cm:house_num") ? "" + tripletMap.get("cm:house_num").getValue(): "";
        String sqlQuery = sqlQueryTemplate.replace(":houseNum", houseNum);
        String parentObj = tripletMap.containsKey("cm:ao_guid") ? "" + tripletMap.get("cm:ao_guid").getValue(): "00000000-0000-0000-0000-000000000000";
        sqlQuery = sqlQuery.replace(":parentObj", parentObj);

        return sqlQuery ;
    }
}
