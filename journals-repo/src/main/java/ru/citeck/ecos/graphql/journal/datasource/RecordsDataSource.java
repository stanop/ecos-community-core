package ru.citeck.ecos.graphql.journal.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.journal.response.JournalData;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.journals.records.JournalRecordsResult;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.query.SortBy;
import ru.citeck.ecos.records.source.alfnode.AlfNodesRecordsDAO;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.source.alfnode.CriteriaAlfNodesSearch;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecordsDataSource implements JournalDataSource {

    @Autowired
    private RecordsService recordsService;

    private String sourceId = AlfNodesRecordsDAO.ID;

    @Override
    public JGqlRecordsConnection getRecords(GqlContext context,
                                            String query,
                                            String language,
                                            JGqlPageInfoInput pageInfo) {

        JournalRecordsResult recordsResult = queryIds(context, query, language, pageInfo);

        JGqlRecordsConnection result = new JGqlRecordsConnection();

        result.setRecords(recordsResult.records
                                       .stream()
                                       .map(r -> recordsService.getMetaValue(context, r))
                                       .flatMap( o -> o.map(Stream::of).orElseGet(Stream::empty))
                                       .collect(Collectors.toList()));

        result.setTotalCount(recordsResult.totalCount);
        result.pageInfo().setHasNextPage(recordsResult.hasNext);
        result.pageInfo().set(pageInfo);

        return result;
    }

    @Override
    public String getServerId() {
        return null;
    }

    @Override
    public boolean isSupportsSplitLoading() {
        return true;
    }

    @Override
    public JournalRecordsResult queryIds(GqlContext context,
                                         String query,
                                         String language,
                                         JGqlPageInfoInput pageInfo) {

        RecordsQuery recordsQuery = new RecordsQuery();
        recordsQuery.setQuery(query);
        if (StringUtils.isBlank(language)) {
            recordsQuery.setLanguage(CriteriaAlfNodesSearch.LANGUAGE);
        } else {
            recordsQuery.setLanguage(language);
        }
        recordsQuery.setMaxItems(pageInfo.getMaxItems());
        recordsQuery.setSortBy(pageInfo.getSortBy()
                .stream()
                .map(sort -> new SortBy(sort.getAttribute(), sort.isAscending()))
                .collect(Collectors.toList()));
        recordsQuery.setSkipCount(pageInfo.getSkipCount());
        recordsQuery.setConsistency(QueryConsistency.EVENTUAL);

        RecordsResult records = recordsService.getRecords(sourceId, recordsQuery);

        return new JournalRecordsResult(records.getRecords(),
                                        records.hasMore(),
                                        records.getTotalCount(),
                                        records.getQuery().getSkipCount(),
                                        records.getQuery().getMaxItems());
    }

    @Override
    public List<MetaValue> convertToGqlValue(GqlContext context,
                                             List<RecordRef> recordsList) {
        return recordsList.stream()
                          .map(r -> recordsService.getMetaValue(context, r))
                          .flatMap( o -> o.map(Stream::of).orElseGet(Stream::empty))
                          .collect(Collectors.toList());
    }

    @Override
    public JournalData queryMetadata(String gqlQuery,
                                     String dataSourceBeanName,
                                     JournalRecordsResult recordsResult) {

        Map<RecordRef, JsonNode> meta = recordsService.getMeta(recordsResult.records, gqlQuery);

        JournalData.JournalRecords journalRecords = new JournalData.JournalRecords();
        List<Object> records = new ArrayList<>();
        meta.forEach((recordRef, data) -> {
            if (data instanceof ObjectNode) {
                ((ObjectNode) data).put("id", recordRef.toString());
            }
            records.add(data);
        });
        journalRecords.setRecords(records);

        JournalData.PageInfo pageInfo = new JournalData.PageInfo();
        pageInfo.setHasNextPage(recordsResult.hasNext);
        pageInfo.setMaxItems(recordsResult.maxItems);
        pageInfo.setSkipCount(recordsResult.skipCount);
        journalRecords.setPageInfo(pageInfo);

        journalRecords.setTotalCount(recordsResult.totalCount);

        JournalData result = new JournalData();
        JournalData.Data data = new JournalData.Data();
        data.setJournalRecords(journalRecords);
        result.setData(data);

        return result;
    }

    @Override
    public Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName) {
        return Optional.empty();
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceId() {
        return sourceId;
    }
}
