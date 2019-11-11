package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.menu.dto.Element;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;

public abstract class AbstractJournalsResolver extends AbstractMenuItemsResolver {

    protected static final String LIST_ID_KEY = "listId";
    private static final String JOURNAL_REF_KEY = "journalRef";
    private static final String JOURNAL_LINK_KEY = "JOURNAL_LINK";

    private JournalService journalService;

    public AbstractJournalsResolver() {
    }

    public void invalidateAll() {
    }

    public void invalidate(String journalId) {
    }

    protected Element constructItem(NodeRef journalRef, Map<String, String> params, Element context) {
        /* get data */
        String title = RepoUtils.getProperty(journalRef, ContentModel.PROP_TITLE , nodeService);
        String journalId = RepoUtils.getProperty(journalRef, JournalsModel.PROP_JOURNAL_TYPE , nodeService);
        String elemIdVar = toUpperCase(journalId);
        String parentElemId = StringUtils.defaultString(context.getId());
        String elemId = String.format("%s_%s_JOURNAL", parentElemId, elemIdVar);
        Boolean displayCount = Boolean.parseBoolean(getParam(params, context, "displayCount"));
        String countForJournalsParam = getParam(params, context, "countForJournals");
        Set<String> countForJournals;
        if (displayCount && StringUtils.isNotEmpty(countForJournalsParam)) {
            countForJournals = new HashSet<>(Arrays.asList(countForJournalsParam.split(",")));
            displayCount = countForJournals.contains(journalId);
        }
        Boolean displayIcon = context.getParams().containsKey("rootElement");

        /* icon. if journal element is placed in root category */
        String icon = null;
        if (displayIcon) {
            icon = journalId;
        }
        /* put all action params from parent (siteName or listId) */
        Map<String, String> actionParams = new HashMap<>();
        if (context.getAction() != null) {
            Map<String, String> parentActionParams = context.getAction().getParams();
            actionParams.putAll(parentActionParams);
        }
        if (MapUtils.isNotEmpty(params) && params.containsKey(LIST_ID_KEY)) {
            actionParams.put(LIST_ID_KEY, params.get(LIST_ID_KEY));
        }

        /* current element action params */
        actionParams.put(JOURNAL_REF_KEY, journalRef.toString());

        /* current element params */
        Map<String, String> elementParams = new HashMap<>();

        /* additional params for constructing child items */
        elementParams.put(JOURNAL_ID_KEY, journalId);

        /* write to element */
        Element element = new Element();
        element.setId(elemId);
        element.setLabel(title);
        element.setIcon(icon);
        element.setAction(JOURNAL_LINK_KEY, actionParams);
        element.setParams(elementParams);
        return element;
    }

    private Long journalItemsCount(RequestKey requestKey) {
        String journalId = requestKey.getJournalId();
        return journalService.getRecordsCount(journalId);
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }

    private class RequestKey {

        private final String userName;
        private final String journalId;

        public RequestKey(String journalId) {
            Objects.requireNonNull(journalId);
            this.userName = StringUtils.defaultString(AuthenticationUtil.getFullyAuthenticatedUser());
            this.journalId = journalId;
        }

        public RequestKey(String userName, String journalId) {
            Objects.requireNonNull(journalId);
            Objects.requireNonNull(userName);
            this.userName = userName;
            this.journalId = journalId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RequestKey that = (RequestKey) o;
            return Objects.equals(userName, that.userName) &&
                    Objects.equals(journalId, that.journalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userName, journalId);
        }

        public String getUserName() {
            return userName;
        }

        public String getJournalId() {
            return journalId;
        }

    }
}
