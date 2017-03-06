package ru.citeck.ecos.config;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ConfigModel;

public class EcosConfigService {

    private static final Log LOGGER = LogFactory.getLog(EcosConfigService.class);

    private SearchService searchService;

    private NodeService nodeService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public Object getParamValue(String key) {
        String result = null;
        try {

            SearchParameters searchParameters = new SearchParameters();
            searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
            searchParameters.setLimitBy(LimitBy.UNLIMITED);
            searchParameters.setLimit(0);
            searchParameters.setMaxPermissionChecks(Integer.MAX_VALUE);
            searchParameters.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
            searchParameters.setMaxItems(-1);
            searchParameters.setQuery("TYPE:\"" + ConfigModel.TYPE_ECOS_CONFIG + "\" AND =@" + ConfigModel.PROP_KEY + ":" + key);
            ResultSet searchResults = searchService.query(searchParameters);
            if (searchResults.getNodeRefs() != null && !searchResults.getNodeRefs().isEmpty()) {
                for (NodeRef settingNodeRef : searchResults.getNodeRefs()) {
                    result = ((String) nodeService.getProperty(settingNodeRef, ConfigModel.PROP_VALUE));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while getting config property " + key, e);
            throw e;
        }
        return result;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}
