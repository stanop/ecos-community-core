package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.menu.dto.Element;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JournalFiltersResolver implements MenuItemsResolver {

    private static final String ID = "JOURNAL_FILTERS";

    private SearchService searchService;
    private NodeService nodeService;

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        String journal = context.getContextId();
        return constructItems(queryFilterRefs(journal));
    }

    private List<Element> constructItems(List<NodeRef> filterRefs) {
        List<Element> elements = new ArrayList<>();
        filterRefs.forEach(filterRef -> elements.add(constructItem(filterRef)));
        return elements;
    }

    private Element constructItem(NodeRef filterRef) {
        Element element = new Element();
        String title = RepoUtils.getProperty(filterRef, ContentModel.PROP_TITLE, nodeService);
        String filterName = RepoUtils.getProperty(filterRef, ContentModel.PROP_NAME, nodeService);
        element.setLabel(title);
        element.setContextId(filterName);
        return element;
    }

    private List<NodeRef> queryFilterRefs(String journal) {
        if (StringUtils.isEmpty(journal)) {
            return Collections.emptyList();
        }
        return FTSQuery.create()
                .type(JournalsModel.TYPE_FILTER).and()
                .exact(JournalsModel.PROP_JOURNAL_TYPES, journal)
                .transactional().query(searchService);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

}
