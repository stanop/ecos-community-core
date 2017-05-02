package ru.citeck.ecos.job.status;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.lucene.LuceneUtils;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Pavel Simonov
 */
public class ChangeStatusByDateWork {

    private static final Log logger = LogFactory.getLog(ChangeStatusJob.class);

    private static final String TYPE_QUERY = "TYPE:\"%s\"";
    private static final String ASPECT_QUERY = "ASPECT:\"%s\"";
    private static final String DATE_QUERY = "@%s:[MIN TO \"%s\"]";
    private static final String STATUS_FIELD = "@icase\\:caseStatusAssoc_added:";

    private QName className;
    private boolean classIsAspect;

    private List<String> includeStatuses = null;
    private List<String> excludeStatuses = null;

    private QName dateField;
    private String dateOffset;
    private int datePrecision;

    private String targetStatus;

    private NodeService nodeService;
    private SearchService searchService;
    private CaseStatusService caseStatusService;
    private ChangeStatusJobRegistry registry;
    private DictionaryService dictionaryService;

    private boolean enabled = true;

    public void init() {
        ParameterCheck.mandatory("targetStatus", targetStatus);
        ParameterCheck.mandatory("dateField", dateField);
        ParameterCheck.mandatory("className", className);
        registry.registerWork(this);

        datePrecision = TimeUtils.getDurationPrecision(dateOffset);
        classIsAspect = dictionaryService.getClass(className).isAspect();
    }

    public List<NodeRef> queryNodes() {

        Date dateSearchValue = getDateSearchValue();
        String query = buildQuery(dateSearchValue);

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setLimit(0);
        searchParameters.setLimitBy(LimitBy.UNLIMITED);
        searchParameters.setMaxItems(-1);
        searchParameters.setMaxPermissionChecks(Integer.MAX_VALUE);
        searchParameters.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL_IF_POSSIBLE);
        searchParameters.setQuery(query);

        ResultSet resultSet;
        try {
            resultSet = searchService.query(searchParameters);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Nodes search failed. Query: '" + query + "'", e);
        }
        try {
            return filterNodes(resultSet.getNodeRefs(), dateSearchValue);
        } finally {
            resultSet.close();
        }
    }

    private List<NodeRef> filterNodes(List<NodeRef> nodeRefs, Date dateSearchValue) {
        List<NodeRef> result = new ArrayList<>();
        for (NodeRef nodeRef : nodeRefs) {
            if (checkDate(nodeRef, dateSearchValue) && checkStatus(nodeRef)
                                                    && checkType(nodeRef)) {
                result.add(nodeRef);
            }
        }
        return result;
    }

    private boolean checkType(NodeRef nodeRef) {
        if (classIsAspect) {
            return nodeService.hasAspect(nodeRef, className);
        } else {
            return className.equals(nodeService.getType(nodeRef));
        }
    }

    private boolean checkDate(NodeRef nodeRef, Date dateSearchValue) {
        Date nodeDate = (Date) nodeService.getProperty(nodeRef, dateField);
        if (nodeDate != null) {
            nodeDate = DateUtils.truncate(nodeDate, datePrecision);
            return nodeDate.getTime() <= dateSearchValue.getTime();
        }
        return false;
    }

    private boolean checkStatus(NodeRef nodeRef) {
        if (includeStatuses == null && excludeStatuses == null) {
            return true;
        }
        String status = caseStatusService.getStatus(nodeRef);
        return !targetStatus.equals(status) &&
               (includeStatuses == null || includeStatuses.contains(status)) &&
               (excludeStatuses == null || !excludeStatuses.contains(status));
    }

    private String buildQuery(Date dateSearchValue) {
        StringBuilder sb = new StringBuilder();
        addQuery(sb, getTypeQuery());
        addQuery(sb, getStatusQuery());
        addQuery(sb, getDateQuery(dateSearchValue));
        return sb.toString();
    }

    private void addQuery(StringBuilder sb, String value) {
        if (!value.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" AND (").append(value).append(")");
            } else {
                sb.append("(").append(value).append(")");
            }
        }
    }

    private String getStatusQuery() {
        String include = buildStatusesQuery(includeStatuses, true, " OR ");
        String exclude = buildStatusesQuery(excludeStatuses, false, " AND ");
        if (include.isEmpty() && exclude.isEmpty()) {
            return "";
        } else if (!include.isEmpty() && !exclude.isEmpty()) {
            return String.format("(%s) AND (%s)", include, exclude);
        } else {
            return include.isEmpty() ? exclude : include;
        }
    }

    private String buildStatusesQuery(List<String> statuses, boolean required, String delimiter) {
        if (statuses == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String status : statuses) {
            NodeRef statusRef = getStatus(status);
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            if (!required) {
                sb.append("NOT ");
            }
            sb.append(STATUS_FIELD).append('"').append(statusRef).append('"');
        }
        return sb.toString();
    }

    private String getDateQuery(Date date) {
        return String.format(DATE_QUERY, dateField, ISO8601DateFormat.format(date));
    }

    private Date getDateSearchValue() {
        return TimeUtils.shiftDate(DateUtils.truncate(new Date(), datePrecision), dateOffset);
    }

    private String getTypeQuery() {
        if (classIsAspect) {
            return String.format(ASPECT_QUERY, className);
        } else {
            return String.format(TYPE_QUERY, className);
        }
    }

    @Override
    public String toString() {
        return String.format("ChangeStatusData:[className:%s, includeStatuses:%s, excludeStatuses:%s, " +
                             "dateField:%s, offset:%s, targetStatus:%s]",
                             className, includeStatuses, excludeStatuses, dateField, dateOffset, targetStatus);
    }

    private NodeRef getStatus(String name) {
        NodeRef statusRef = caseStatusService.getStatusByName(name);
        if (statusRef == null) {
            throw new AlfrescoRuntimeException("Status not found: " + name);
        }
        return statusRef;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setIncludeStatuses(List<String> includeStatuses) {
        this.includeStatuses = includeStatuses;
    }

    public void setExcludeStatuses(List<String> excludeStatuses) {
        this.excludeStatuses = excludeStatuses;
    }

    public void setDateField(QName dateField) {
        this.dateField = dateField;
    }

    public void setDateOffset(String dateOffset) {
        this.dateOffset = dateOffset;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setCaseStatusService(CaseStatusService caseStatusService) {
        this.caseStatusService = caseStatusService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setChangeStatusRegistry(ChangeStatusJobRegistry changeStatusRegistry) {
        this.registry = changeStatusRegistry;
    }
}
