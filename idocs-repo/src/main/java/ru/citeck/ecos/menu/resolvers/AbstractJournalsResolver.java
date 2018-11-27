package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.menu.dto.Element;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.processor.TemplateExpressionEvaluator;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractJournalsResolver extends AbstractMenuItemsResolver {

    private static final String JOURNAL_REF_KEY = "journalRef";
    private static final String JOURNAL_LINK_KEY = "JOURNAL_LINK";
    private static final Integer COUNT_MAX = 100;
    private static final String CRIT_ELEM_FORMAT = "\"%s_%d\":\"%s\"";
    private static final JGqlPageInfoInput PAGE_INFO = new JGqlPageInfoInput(null, COUNT_MAX,
            Collections.emptyList(), 0);

    private TemplateExpressionEvaluator expressionEvaluator;
    private NamespaceService namespaceService;
    private JournalService journalService;

    protected Element constructItem(NodeRef journalRef, Map<String, String> params, Element context) {
        /* get data */
        String title = RepoUtils.getProperty(journalRef, ContentModel.PROP_TITLE , nodeService);
        String journalId = RepoUtils.getProperty(journalRef, JournalsModel.PROP_JOURNAL_TYPE , nodeService);
        String elemIdVar = toUpperCase(journalId);
        String parentElemId = StringUtils.defaultString(context.getId());
        String elemId = String.format("%s_%s_JOURNAL", parentElemId, elemIdVar);
        Boolean displayCount = Boolean.parseBoolean(getParam(params, context, "displayCount"));
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
        } else {
            Map<String, String> props = context.getParams();
            if (MapUtils.isNotEmpty(props)) {
                actionParams.putAll(props);
            }
        }
        /* current element action params */
        actionParams.put(JOURNAL_REF_KEY, journalRef.toString());

        /* current element params */
        Map<String, String> elementParams = new HashMap<>();

        /* badge (items count) */
        if (displayCount) {
            Long count = journalItemsCount(journalRef, journalId);
            String badge = count < COUNT_MAX ? count.toString() : "99+";
            elementParams.put("badge", badge);
        }

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

    private Long journalItemsCount(NodeRef journalRef, String journalId) {
        String query = buildJournalQuery(journalRef);
        RecordsResult<RecordRef> result = journalService.getRecords(journalId, query, null, PAGE_INFO);
        return result.getTotalCount();
    }

//    TODO: move this to journalService
    private String buildJournalQuery(NodeRef journalRef) {
        StringBuilder sb = new StringBuilder("{");
        List<NodeRef> criteria = RepoUtils.getChildrenByAssoc(journalRef, JournalsModel.ASSOC_SEARCH_CRITERIA, nodeService);
        if (CollectionUtils.isEmpty(criteria)) {
            return "{}";
        }
        for (int i = 0; i < criteria.size(); i++ ) {
            QName fieldQName = RepoUtils.getProperty(criteria.get(i), JournalsModel.PROP_FIELD_QNAME, nodeService);
            String predicate = RepoUtils.getProperty(criteria.get(i), JournalsModel.PROP_PREDICATE, nodeService);
            String criterionValue = RepoUtils.getProperty(criteria.get(i), JournalsModel.PROP_CRITERION_VALUE, nodeService);
            String criterion = buildCriterion(fieldQName, predicate, criterionValue, i);
            if (i != 0 && StringUtils.isNotEmpty(criterion)) {
                sb.append(",");
            }
            sb.append(criterion);
        }
        sb.append("}");
        return sb.toString();
    }

//    TODO: move this to journalService
    private String buildCriterion(QName fieldQName, String predicate, String criterionValue, int i) {
        if (fieldQName == null || predicate == null || criterionValue == null) {
            return "";
        }
        String field = fieldQName.toPrefixString(namespaceService);
        String criterion = "";
        if (StringUtils.isNotEmpty(criterionValue)) {
            Map<String, Object> model = new HashMap<>();
            /* this replacement is used to fix template strings like this one:
             * <#list (people.getContainerGroups(person)![]) as group>#{group.nodeRef},</#list>#{person.nodeRef} */
            criterionValue = criterionValue.replace("#{", "${");
            criterion = (String) expressionEvaluator.evaluate(criterionValue, model);
        }
        String f = String.format(CRIT_ELEM_FORMAT, "field", i, field);
        String p = String.format(CRIT_ELEM_FORMAT, "predicate", i, predicate);
        String v = String.format(CRIT_ELEM_FORMAT, "value", i, criterion);
        return String.format("%s,%s,%s", f, p, v);
    }

    @Autowired
    public void setExpressionEvaluator(TemplateExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }

    @Autowired
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
}
