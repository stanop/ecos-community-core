package ru.citeck.ecos.currency;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.node.DoubleNode;
import lombok.Data;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.records2.predicate.PredicateService;
import ru.citeck.ecos.records2.predicate.model.AndPredicate;
import ru.citeck.ecos.records2.predicate.model.Predicates;
import ru.citeck.ecos.records.source.alf.AlfNodesRecordsDAO;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.MutableRecordsLocalDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsMetaLocalDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsQueryWithMetaLocalDAO;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CurrencyRateRecordsDAO extends LocalRecordsDAO implements
        RecordsQueryWithMetaLocalDAO<CurrencyRateRecordsDAO.CurrencyRateRecord>,
        RecordsMetaLocalDAO<CurrencyRateRecordsDAO.CurrencyRateRecord>,
        MutableRecordsLocalDAO<CurrencyRateRecordsDAO.CurrencyRateRecord> {

    private static final String ID = "currency-rate";

    private AlfNodesRecordsDAO alfNodesRecordsDAO;
    private CurrencyService currencyService;

    @Autowired
    public CurrencyRateRecordsDAO(AlfNodesRecordsDAO alfNodesRecordsDAO,
                                  CurrencyService currencyService) {
        this.alfNodesRecordsDAO = alfNodesRecordsDAO;
        this.currencyService = currencyService;

        setId(ID);
    }

    @Override
    public List<CurrencyRateRecord> getMetaValues(List<RecordRef> list) {
        return list.stream()
                .map(recordRef -> RecordRef.create(AlfNodesRecordsDAO.ID, recordRef.getId()))
                .map(recordRef -> recordsService.getMeta(recordRef, CurrencyRateRecord.class))
                .collect(Collectors.toList());
    }

    @Override
    public RecordsQueryResult<CurrencyRateRecord> getMetaValues(RecordsQuery recordsQuery) {
        RecordsQueryResult<RecordRef> records = queryRecords(recordsQuery);

        RecordsQueryResult<CurrencyRateRecord> result = new RecordsQueryResult<>();
        result.merge(records);
        result.setHasMore(records.getHasMore());
        result.setTotalCount(records.getTotalCount());
        result.setRecords(getMetaValues(records.getRecords()));

        if (recordsQuery.isDebug()) {
            result.setDebugInfo(getClass(), "query", recordsQuery.getQuery());
            result.setDebugInfo(getClass(), "language", recordsQuery.getLanguage());
        }

        return result;
    }

    @Override
    public List<CurrencyRateRecord> getValuesToMutate(List<RecordRef> list) {
        return getMetaValues(list);
    }

    @Override
    public RecordsMutResult save(List<CurrencyRateRecord> list) {
        RecordsMutation recordsMutation = new RecordsMutation();

        list.forEach(currencyRate -> {
            AndPredicate predicate = Predicates.and(
                    Predicates.eq("TYPE", IdocsModel.TYPE_CURRENCY_RATE_RECORD.toString()),
                    Predicates.eq("idocs:crrSyncDate", currencyRate.syncDate),
                    Predicates.eq("idocs:crrBaseCurrency", getRefByCode(currencyRate.baseCurrencyCode).toString()),
                    Predicates.eq("idocs:crrTargetCurrency", getRefByCode(currencyRate.targetCurrencyCode).toString())
            );

            RecordsQuery query = new RecordsQuery();
            query.setLanguage(PredicateService.LANGUAGE_PREDICATE);
            query.setQuery(predicate);

            Optional<RecordRef> recordRef = recordsService.queryRecord(query);
            if (recordRef.isPresent()) {
                recordsMutation.getRecords().add(composeCurrencyRecordMeta(recordRef.get().getId(), currencyRate));
            } else {
                recordsMutation.getRecords().add(composeCurrencyRecordMeta(null, currencyRate));
            }
        });

        return alfNodesRecordsDAO.mutate(recordsMutation);
    }

    private RecordMeta composeCurrencyRecordMeta(String id, CurrencyRateRecord currencyRate) {
        RecordMeta recordMeta = new RecordMeta(id);

        recordMeta.set("_type", "idocs:currencyRateRecord");
        recordMeta.set("_parent", "/app:company_home/app:dictionary/cm:dataLists/cm:currency-rates");
        recordMeta.set("_parentAtt", "cm:contains");

        recordMeta.set("idocs:crrValue", new DoubleNode(currencyRate.rate));
        recordMeta.set("idocs:crrDate", currencyRate.date);
        recordMeta.set("idocs:crrSyncDate", currencyRate.syncDate);
        recordMeta.set("idocs:crrBaseCurrency", getRefByCode(currencyRate.baseCurrencyCode).toString());
        recordMeta.set("idocs:crrTargetCurrency", getRefByCode(currencyRate.targetCurrencyCode).toString());

        return recordMeta;
    }

    private NodeRef getRefByCode(String currencyCode) {
        Currency currency = currencyService.getCurrencyByCode(currencyCode);
        if (currency == null) {
            throw new IllegalArgumentException("Currency not found for code " + currencyCode);
        }
        return currency.getNodeRef();
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion recordsDeletion) {
        return alfNodesRecordsDAO.delete(recordsDeletion);
    }

    @Data
    public static class CurrencyRateRecord {
        private double rate;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone = "GMT")
        @ecos.com.fasterxml.jackson210.annotation.JsonFormat(
            shape = ecos.com.fasterxml.jackson210.annotation.JsonFormat.Shape.STRING,
            pattern = "dd.MM.yyyy",
            timezone = "GMT"
        )
        private Date date;
        private String syncDate;
        private String baseCurrencyCode;
        private String targetCurrencyCode;
    }

}
