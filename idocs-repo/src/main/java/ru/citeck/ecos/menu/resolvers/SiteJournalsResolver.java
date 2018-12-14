package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.menu.dto.Element;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SiteJournalsResolver extends AbstractJournalsResolver {

    private static final String ID = "SITE_JOURNALS";
    private static final String JOURNAL_NAME_TEMPLATE = "site-%s-%s"; /* site-<sitename>-<listId> */
    private static final String DEFAULT_JOURNAL_LIST_ID = "main";

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        String siteId = getParam(params, context, SITE_ID_KEY);
        String listId = getParam(params, context, LIST_ID_KEY);
        if (StringUtils.isNotEmpty(listId)) {
            params.put(LIST_ID_KEY, listId);
        } else {
            params.put(LIST_ID_KEY, DEFAULT_JOURNAL_LIST_ID);
            listId = DEFAULT_JOURNAL_LIST_ID;
        }
        return getJournalsBySiteId(siteId, listId).stream()
                .map(nodeRef -> constructItem(nodeRef, params, context))
                .collect(Collectors.toList());
    }

    private List<NodeRef> getJournalsBySiteId(String siteId, String listId) {
        if (StringUtils.isEmpty(siteId)) {
            return Collections.emptyList();
        }
        return FTSQuery.create()
                        .type(JournalsModel.TYPE_JOURNALS_LIST).and()
                        .value(ContentModel.PROP_NAME, String.format(JOURNAL_NAME_TEMPLATE, siteId, listId))
                        .transactional().query(searchService).stream()
                        .flatMap(listRef -> nodeService.getTargetAssocs(listRef, JournalsModel.ASSOC_JOURNALS).stream())
                        .map(AssociationRef::getTargetRef)
                        .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return ID;
    }

}
