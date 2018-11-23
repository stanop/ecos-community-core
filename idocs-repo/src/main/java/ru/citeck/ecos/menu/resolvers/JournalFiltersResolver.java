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
import java.util.stream.Collectors;

@Component
public class JournalFiltersResolver extends AbstractMenuItemsResolver {

    private static final String ID = "JOURNAL_FILTERS";
    private static final String FILTER_REF_KEY = "filterRef";
    private static final String FILTER_LINK_KEY = "FILTER_LINK";
    private static final String FILTER_ID_KEY = "filterId";

//    private SearchService searchService;
//    private NodeService nodeService;

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        String journal = getParam(params, context, JOURNAL_ID_KEY);
        String engJournalTitle = getParam(params, context, ENG_JOURNAL_TITLE_KEY);

        return queryFilterRefs(journal).stream()
                .map(filterRef -> constructItem(filterRef, context, engJournalTitle))
                .collect(Collectors.toList());

//        return constructItems(queryFilterRefs(journal));
    }

//    private List<Element> constructItems(List<NodeRef> filterRefs) {
//        List<Element> elements = new ArrayList<>();
//        filterRefs.forEach(filterRef -> elements.add(constructItem(filterRef, context)));
//        return elements;
//    }

    private Element constructItem(NodeRef filterRef, Element context, String engJournalTitle) {

        String engFilterTitle = getUppercaseEngTitle(filterRef);
        String id = String.format("HEADER_%s_%s_FILTER", engJournalTitle, engFilterTitle);

        String title = RepoUtils.getProperty(filterRef, ContentModel.PROP_TITLE, nodeService);
        String filterName = RepoUtils.getProperty(filterRef, ContentModel.PROP_NAME, nodeService);

        Map<String, String> parentActionParams = context.getAction().getParams();
        Map<String, String> actionParams = new HashMap<>(parentActionParams);
        actionParams.put(FILTER_REF_KEY, filterRef.toString());

        Element element = new Element();
        element.setId(id);
        element.setLabel(title);
        element.setAction(FILTER_LINK_KEY, actionParams);
        /* additional params for constructing child items */
        Map<String, String> elementParams = new HashMap<>();
        elementParams.put(FILTER_ID_KEY, filterName);
        element.setParams(elementParams);
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

//    @Autowired
//    public void setSearchService(SearchService searchService) {
//        this.searchService = searchService;
//    }
//
//    @Autowired
//    public void setNodeService(NodeService nodeService) {
//        this.nodeService = nodeService;
//    }

}
