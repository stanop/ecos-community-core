package ru.citeck.ecos.reports;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.predicate.PredicateService;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDao;
import ru.citeck.ecos.records2.source.dao.local.v2.LocalRecordsQueryWithMetaDao;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class ReportsDataRecords extends LocalRecordsDao implements LocalRecordsQueryWithMetaDao<ReportsDataRecords.ReportResult> {

    private static final String ID = "reports-data";
    private static final int DEFAULT_MAX_ITEMS = 1000;

    private final RecordsService recordsService;

    @Autowired
    public ReportsDataRecords(RecordsService recordsService) {
        setId(ID);
        this.recordsService = recordsService;
    }


    @Override
    public RecordsQueryResult<ReportResult> queryLocalRecords(RecordsQuery query, MetaField field) {

        String language = query.getLanguage();
        if (!PredicateService.LANGUAGE_PREDICATE.equals(language)) {
            return new RecordsQueryResult<>();
        }

        if (query.getMaxItems() == -1) {
            query.setMaxItems(DEFAULT_MAX_ITEMS);
        }

        RecordsQuery alfNodeQuery = new RecordsQuery(query);
        alfNodeQuery.setSourceId("");
        alfNodeQuery.setGroupBy(Collections.emptyList());

        Set<String> attributes = new TreeSet<>(query.getGroupBy());

        RecordsQueryResult<RecordMeta> foundRecords = recordsService.queryRecords(alfNodeQuery, attributes);

        Map<ObjectData, AtomicInteger> attributeGroups = new HashMap<>();

        foundRecords.getRecords().forEach(r -> {
            ObjectData atts = r.getAttributes();

            if (attributeGroups.get(atts) == null) {
                attributeGroups.put(atts, new AtomicInteger(1));
            } else {
                attributeGroups.get(atts).getAndIncrement();
            }
        });


        RecordsQueryResult<ReportResult> result = new RecordsQueryResult<>();

        List<ReportResult> collect = attributeGroups.entrySet().stream().map((e) -> {
            ReportResult reportResult = new ReportResult();
            reportResult.setGroupAtts(e.getKey());
            reportResult.setCount(e.getValue().get());
            reportResult.setId(UUID.randomUUID().toString());
            return reportResult;
        }).collect(Collectors.toList());

        result.setRecords(collect);
        result.setTotalCount(foundRecords.getTotalCount());

        return result;
    }

    @Override
    public boolean isGroupingSupported() {
        return true;
    }

    @Data
    public static class ReportResult {
        private String id;
        private int count;
        private ObjectData groupAtts;
    }

}
