package ru.citeck.ecos.config;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.search.*;

public class EcosConfigSearchService {

    private static final Log LOGGER = LogFactory.getLog(EcosConfigSearchService.class);

    private static final String CONFIG_NAMESPASE = "http://www.citeck.ru/model/config/1.0";

    private SearchCriteriaFactory searchCriteriaFactory;

    private CriteriaSearchService criteriaSearchService;

    private NodeService nodeService;

    public void setSearchCriteriaFactory(SearchCriteriaFactory searchCriteriaFactory) {
        this.searchCriteriaFactory = searchCriteriaFactory;
    }

    public void setCriteriaSearchService(CriteriaSearchService criteriaSearchService) {
        this.criteriaSearchService = criteriaSearchService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public Object getParamValue(String key) {
        String result = null;
        try {
            SearchCriteria searchCriteria = searchCriteriaFactory.createSearchCriteria();
            searchCriteria.addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, QName.createQName(CONFIG_NAMESPASE, "ecosConfig"))
                    .addCriteriaTriplet(QName.createQName(CONFIG_NAMESPASE, "key"), SearchPredicate.STRING_EQUALS, key);
            CriteriaSearchResults searchResults = criteriaSearchService.query(searchCriteria, SearchService.LANGUAGE_LUCENE);
            if (searchResults.getResults() != null && !searchResults.getResults().isEmpty()) {
                for (NodeRef settingNodeRef : searchResults.getResults()) {
                    result = ((String) nodeService.getProperty(settingNodeRef, QName.createQName(CONFIG_NAMESPASE, "value")));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while getting config property " + key, e);
            throw e;
        }
        return result;
    }
}
