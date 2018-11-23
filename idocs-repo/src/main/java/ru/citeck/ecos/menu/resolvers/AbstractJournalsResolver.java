package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.collections.MapUtils;
import ru.citeck.ecos.menu.dto.Element;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractJournalsResolver extends AbstractMenuItemsResolver {

    private static final String JOURNAL_REF_KEY = "journalRef";
    private static final String JOURNAL_LINK_KEY = "JOURNAL_LINK";

    protected Element constructItem(NodeRef journalRef, Element context) {
        /* get data */
        String title = RepoUtils.getProperty(journalRef, ContentModel.PROP_TITLE , nodeService);
        String name = RepoUtils.getProperty(journalRef, ContentModel.PROP_NAME , nodeService);

        String engJournalTitle = getUppercaseEngTitle(journalRef);
        String parentId = context.getId();
        String id = String.format("%s_%s_JOURNAL", parentId, engJournalTitle);

        Map<String, String> actionParams = new HashMap<>();
        if (context.getAction() != null) {
            Map<String, String> parentActionParams = context.getAction().getParams();
            actionParams.putAll(parentActionParams);
        } else {
            Map<String, String> props = context.getParams();
            if (MapUtils.isNotEmpty(props)) {
                actionParams.putAll(props);
            }
        }
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

}
