package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.menu.dto.Item;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JournalFiltersResolver implements MenuItemsResolver {

    private static final String ID = "JOURNAL_FILTERS";

    private SearchService searchService;
    private NodeService nodeService;

    @Override
    public List<Item> resolve(Map<String, String> params) {
        String context = params.get(CONTEXT_PARAM_KEY);
        return constructItems(queryFilterRefs(context));
    }

    private List<Item> constructItems(List<NodeRef> filterRefs) {
        List<Item> items = new ArrayList<>();
        filterRefs.forEach(filterRef -> items.add(constructItem(filterRef)));
        return items;
    }

    private Item constructItem(NodeRef filterRef) {
        Item item = new Item();
        String title = RepoUtils.getProperty(filterRef, ContentModel.PROP_TITLE, nodeService);
        item.setLabel(title);
        return item;
    }

    private List<NodeRef> queryFilterRefs(String journalType) {
        return FTSQuery.create()
                .type(JournalsModel.TYPE_FILTER).and()
                .exact(JournalsModel.PROP_JOURNAL_TYPES, journalType)
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
