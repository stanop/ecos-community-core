package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.menu.dto.Element;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SiteJournalsResolver implements MenuItemsResolver {

    private static final String ID = "SITE_JOURNALS";
    private SearchService searchService;
    private NodeService nodeService;
    private JournalService journalService;
    private static final String JOURNAL_REF_KEY = "journalRef";
    private static final String JOURNAL_LINK_KEY = "JOURNAL_LINK";

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        String siteId = context.getContextId();
        return getJournalsBySiteId(siteId).stream()
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
        element.setAction(JOURNAL_LINK_KEY, actionParams);;
        return element;
    }

    private List<NodeRef> getJournalsBySiteId(String siteId) {
        if (StringUtils.isEmpty(siteId)) {
            return Collections.emptyList();
        }
        return FTSQuery.create()
                        .type(JournalsModel.TYPE_JOURNALS_LIST).and()
                        .value(ContentModel.PROP_NAME, "site-" + siteId + "-main")
                        .transactional().query(searchService).stream()
                        .flatMap(listRef -> nodeService.getTargetAssocs(listRef, JournalsModel.ASSOC_JOURNALS).stream())
                        .map(AssociationRef::getTargetRef)
                        .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
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
