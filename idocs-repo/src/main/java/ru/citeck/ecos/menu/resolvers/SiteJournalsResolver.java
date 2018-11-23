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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SiteJournalsResolver extends AbstractMenuItemsResolver {

    private static final String ID = "SITE_JOURNALS";

//    private SearchService searchService;
//    private NodeService nodeService;
    private static final String JOURNAL_REF_KEY = "journalRef";
    private static final String JOURNAL_LINK_KEY = "JOURNAL_LINK";
    private static final String PAGE_LINK_KEY = "PAGE_LINK";
    private static final String PAGE_ID_KEY = "pageId";

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        String siteId = getParam(params, context, SITE_ID_KEY);
        List<Element> result = getJournalsBySiteId(siteId).stream()
                .map(nodeRef -> constructItem(nodeRef, context, siteId))
                .collect(Collectors.toList());
        result.add(docLibElement(siteId));
        result.add(calendarElement(siteId));
        return result;
    }

    private Element constructItem(NodeRef journalRef, Element context, String siteId) {
        /* get data */
        String title = RepoUtils.getProperty(journalRef, ContentModel.PROP_TITLE , nodeService);
        String name = RepoUtils.getProperty(journalRef, ContentModel.PROP_NAME , nodeService);

        String engJournalTitle = getUppercaseEngTitle(journalRef);
        String id = String.format("HEADER_%s_%s_JOURNAL", siteId.toUpperCase(), engJournalTitle);

//        String id = buildId(journalRef, siteId);
        Map<String, String> parentActionParams = context.getAction().getParams();
        Map<String, String> actionParams = new HashMap<>(parentActionParams);
        actionParams.put(JOURNAL_REF_KEY, journalRef.toString());
        /* write to element */
        Element element = new Element();
        element.setId(id);
        element.setLabel(title);
        element.setAction(JOURNAL_LINK_KEY, actionParams);
        /* additional params for constructing child items */
        Map<String, String> elementParams = new HashMap<>();
        elementParams.put(JOURNAL_ID_KEY, name);
        elementParams.put(ENG_JOURNAL_TITLE_KEY, engJournalTitle);
        element.setParams(elementParams);
        return element;
    }

//    private String buildId(NodeRef journalRef, String siteId) {
//        String engJournalTitle = getEngValue(journalRef, ContentModel.PROP_TITLE)
//                .replaceAll("[^a-zA-Z0-9]", "_");
//        String result = String.format("HEADER_%s_%s_JOURNAL", siteId, engJournalTitle);
//        return result.toUpperCase();
//    }

    private Element docLibElement(String siteId) {
        String id = String.format("HEADER_%s_DOCUMENTLIBRARY", siteId.toUpperCase());
        String pageId = String.format("site/%s/documentlibrary", siteId);
        Map<String, String> actionParams = new HashMap<>();
        actionParams.put(PAGE_ID_KEY, pageId);
        Element element = new Element();
        element.setId(id);
        element.setLabel("menu.item.documentlibrary");
        element.setAction(PAGE_LINK_KEY, actionParams);
        return element;
    }

    private Element calendarElement(String siteId) {
        String id = String.format("HEADER_%s_SITE_CALENDAR", siteId.toUpperCase());
        String pageId = String.format("site/%s/calendar", siteId);
        Map<String, String> actionParams = new HashMap<>();
        actionParams.put(PAGE_ID_KEY, pageId);
        Element element = new Element();
        element.setId(id);
        element.setLabel("menu.item.calendar");
        element.setAction(PAGE_LINK_KEY, actionParams);
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

//    @Autowired
//    public void setNodeService(NodeService nodeService) {
//        this.nodeService = nodeService;
//    }
//
//    @Autowired
//    public void setSearchService(SearchService searchService) {
//        this.searchService = searchService;
//    }
}
