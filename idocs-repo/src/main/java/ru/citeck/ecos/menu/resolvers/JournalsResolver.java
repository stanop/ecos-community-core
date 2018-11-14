package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.menu.dto.Item;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import java.util.*;
import java.util.stream.Collectors;

public class JournalsResolver implements MenuItemsResolver {

    private static final String ID = "JOURNALS";
    private static final String LIST_ID_KEY = "listId";

    private NodeService nodeService;
    private SearchService searchService;
    private JournalService journalService;

    @Override
    public List<Item> resolve(Map<String, String> params) {
        String listId = params.get(LIST_ID_KEY);
        return queryJournalsRefs(listId)
                .map(this::getJournals)
                .map(this::constructItems)
                .orElse(new ArrayList<>());
    }

    private List<String> getJournals(List<NodeRef> nodeRefs) {
        return nodeRefs.stream()
                .map(this::getJournalName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String getJournalName(NodeRef nodeRef) {
        return (String) nodeService.getProperty(nodeRef, JournalsModel.PROP_JOURNAL_TYPE);
    }

    private List<Item> constructItems(List<String> journals) {
        List<Item> items = new ArrayList<>();
        journals.forEach(journal -> items.add(constructItem(journal)));
        return items;
    }

    private Item constructItem(String journal) {
        Item item = new Item();
        String id = journalService.getJournalType(journal).getId();
        item.setId(id);
        return item;
    }

    private Optional<List<NodeRef>> queryJournalsRefs(String journalList) {
        NodeRef parent = new NodeRef("workspace://SpacesStore/journal-meta-f-lists");
        return FTSQuery.create()
                .parent(parent).and()
                .type(JournalsModel.TYPE_JOURNALS_LIST).and()
                .exact(ContentModel.PROP_NAME, journalList)
                .transactional().query(searchService)
                .stream().findFirst()
                .map(this::journalsFromList);
    }

    private List<NodeRef> journalsFromList(NodeRef nodeRef) {
        return nodeService.getTargetAssocs(nodeRef, JournalsModel.ASSOC_JOURNALS).stream()
                .map(AssociationRef::getTargetRef)
                .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }

}
