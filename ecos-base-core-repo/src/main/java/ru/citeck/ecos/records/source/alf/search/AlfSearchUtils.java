package ru.citeck.ecos.records.source.alf.search;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AlfSearchUtils {

    private SearchService searchService;
    private NodeService nodeService;

    @Autowired
    public AlfSearchUtils(SearchService searchService, NodeService nodeService) {
        this.searchService = searchService;
        this.nodeService = nodeService;
    }

    public List<Object> queryFtsDistinctValues(String ftsBaseQuery, QName property, int max) {

        String ftsQuery = "(" + ftsBaseQuery + ")";

        Set<Object> values = new HashSet<>();
        Set<Object> newValues = new HashSet<>();

        int found, requests = 0;
        do {

            SearchParameters parameters = new SearchParameters();
            parameters.setMaxItems(max);
            parameters.setLimit(max);
            parameters.setQuery(ftsQuery);
            parameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            parameters.setQueryConsistency(QueryConsistency.EVENTUAL);

            ResultSet resultSet = null;

            try {

                resultSet = searchService.query(parameters);
                found = resultSet.length();

                newValues.clear();

                for (NodeRef nodeRef : resultSet.getNodeRefs()) {

                    Object value = nodeService.getProperty(nodeRef, property);
                    if (value != null && !values.contains(value)) {
                        newValues.add(value);
                    }
                }

                for (Object value : newValues) {
                    ftsQuery +=  " AND NOT @" + property + ":\"" + value + "\"";
                }

                values.addAll(newValues);

            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }

        } while (found > 0 && values.size() <= max && ++requests <= max);

        return new ArrayList<>(values);
    }
}
