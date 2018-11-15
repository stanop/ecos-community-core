package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.menu.dto.Element;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;

@Component
public class JournalFiltersResolver extends AbstractMenuItemsResolver {

    private static final String ID = "JOURNAL_FILTERS";
    private static final String FILTER_REF_KEY = "filterRef";
    private static final String FILTER_LINK_KEY = "FILTER_LINK";

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
        Map<String, String> actionParams = new HashMap<>();
        actionParams.put(FILTER_REF_KEY, filterRef.toString());
        element.setLabel(title);
        element.setContextId(filterName);
        element.setAction(FILTER_LINK_KEY, actionParams);
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
