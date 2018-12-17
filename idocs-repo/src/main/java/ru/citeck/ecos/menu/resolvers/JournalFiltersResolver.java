package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        String journalId = getParam(params, context, JOURNAL_ID_KEY);

        return queryFilterRefs(journalId).stream()
                .map(filterRef -> constructItem(filterRef, context))
                .collect(Collectors.toList());
    }

    private Element constructItem(NodeRef filterRef, Element context) {
        String title = RepoUtils.getProperty(filterRef, ContentModel.PROP_TITLE, nodeService);
        String elemId = buildElemId(filterRef, context);

        Map<String, String> parentActionParams = context.getAction().getParams();
        Map<String, String> actionParams = new HashMap<>(parentActionParams);
        actionParams.put(FILTER_REF_KEY, filterRef.toString());

        Element element = new Element();
        element.setId(elemId);
        element.setLabel(title);
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

    private String buildElemId(NodeRef filterRef, Element context) {
        String filterName = RepoUtils.getProperty(filterRef, ContentModel.PROP_NAME, nodeService);
        String elemIdVar = toUpperCase(filterName);
        String parentElemId = StringUtils.defaultString(context.getId());
        if (StringUtils.isNotEmpty(parentElemId)) {
            parentElemId = parentElemId.replace("_JOURNAL", "");
        }
        return String.format("%s_%s_FILTER", parentElemId, elemIdVar);
    }

    @Override
    public String getId() {
        return ID;
    }

}
