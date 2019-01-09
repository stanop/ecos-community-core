package ru.citeck.ecos.journals.records;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;
import ru.citeck.ecos.records.request.query.SortBy;
import ru.citeck.ecos.records.source.alfnode.search.CriteriaAlfNodesSearch;

import java.util.stream.Collectors;

public class JournalRecordsDAO {

    private GqlQueryGenerator gqlQueryGenerator;
    private RecordsService recordsService;

    public RecordsResult<ObjectNode> getRecordsWithData(JournalType journalType,
                                                        String query,
                                                        String language,
                                                        JGqlPageInfoInput pageInfo,
                                                        boolean debug) {

        String gqlQuery = gqlQueryGenerator.generate(journalType);
        RecordsQuery recordsQuery = createQuery(journalType.getDataSource(), query, language, pageInfo, debug);

        return recordsService.getRecords(recordsQuery, gqlQuery);
    }

    public RecordsResult<RecordRef> getRecords(JournalType journalType,
                                               String query,
                                               String language,
                                               JGqlPageInfoInput pageInfo,
                                               boolean debug) {

        RecordsQuery recordsQuery = createQuery(journalType.getDataSource(), query, language, pageInfo, debug);
        return recordsService.getRecords(recordsQuery);
    }

    public RecordsQuery createQuery(String sourceId,
                                    String query,
                                    String language,
                                    JGqlPageInfoInput pageInfo,
                                    boolean debug) {

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
        recordsQuery.setSourceId(sourceId);
        recordsQuery.setDebug(debug);

        return recordsQuery;
    }

    public void clearCache() {
        gqlQueryGenerator.clearCache();
    }

    public void setGqlQueryGenerator(GqlQueryGenerator gqlQueryGenerator) {
        this.gqlQueryGenerator = gqlQueryGenerator;
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }
}
