package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
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
import java.util.stream.Collectors;

@Component
public class JournalsResolver extends AbstractMenuItemsResolver {

    private static final String ID = "JOURNALS";
    private static final String LIST_ID_KEY = "listId";
    private static final String JOURNAL_REF_KEY = "journalRef";
    private static final String JOURNAL_LINK_KEY = "JOURNAL_LINK";

    private NodeService nodeService;
    private SearchService searchService;

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        String journalsListId = params.get(LIST_ID_KEY);
        return queryJournalsRefs(journalsListId).stream()
                .map(this::constructItem)
                .collect(Collectors.toList());
    }

    private Element constructItem(NodeRef journalRef) {
        Element element = new Element();
        String title = RepoUtils.getProperty(journalRef, ContentModel.PROP_TITLE , nodeService);
        String name = RepoUtils.getProperty(journalRef, ContentModel.PROP_NAME , nodeService);
        Map<String, String> actionParams = new HashMap<>();
        actionParams.put(JOURNAL_REF_KEY, journalRef.toString());
        element.setId(name);
        element.setLabel(title);
        element.setContextId(name);
        element.setAction(JOURNAL_LINK_KEY, actionParams);
        return element;
    }

    private List<NodeRef> queryJournalsRefs(String journalList) {
        if (StringUtils.isEmpty(journalList)) {
            return Collections.emptyList();
        }
        NodeRef parent = new NodeRef("workspace://SpacesStore/journal-meta-f-lists");
        return FTSQuery.create()
                .parent(parent).and()
                .type(JournalsModel.TYPE_JOURNALS_LIST).and()
                .exact(ContentModel.PROP_NAME, journalList)
                .transactional().query(searchService)
                .stream().findFirst()
                .map(this::journalsFromList)
                .orElse(Collections.emptyList());
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

}
