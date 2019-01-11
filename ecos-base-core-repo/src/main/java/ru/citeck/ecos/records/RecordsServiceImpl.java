package ru.citeck.ecos.records;

import org.apache.commons.beanutils.PropertyUtils;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.records.request.delete.RecordsDelResult;
import ru.citeck.ecos.records.request.delete.RecordsDeletion;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.request.result.RecordsResult;
import ru.citeck.ecos.records.source.MetaAttributeDef;
import ru.citeck.ecos.records.source.RecordsDAO;

import java.beans.PropertyDescriptor;
import java.util.*;

public class RecordsServiceImpl implements RecordsService {

    @Override
    public RecordsQueryResult<RecordRef> getRecords(RecordsQuery query) {


        return null;
    }

    @Override
    public <T> RecordsQueryResult<T> getRecords(RecordsQuery query, Class<T> metaClass) {
        return null;
    }

    @Override
    public RecordsQueryResult<RecordMeta> getRecords(RecordsQuery query, Map<String, String> attributes) {
        return null;
    }

    @Override
    public RecordsQueryResult<RecordMeta> getRecords(RecordsQuery query,
                                                     Collection<String> attributes) {
        return null;
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(Collection<RecordRef> records,
                                             Collection<String> attributes) {

        return getMeta(new ArrayList<>(records), attributes);
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(List<RecordRef> records,
                                             Collection<String> attributes) {

        Map<String, String> attributesMap = new HashMap<>();
        for (String attribute : attributes) {
            attributesMap.put(attribute, attribute);
        }
        return getMeta(records, attributesMap);
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(Collection<RecordRef> records,
                                             Map<String, String> attributes) {

        return getMeta(new ArrayList<>(records), attributes);
    }

    @Override
    public <T> RecordsResult<T> getMeta(Collection<RecordRef> records,
                                        Class<T> metaClass) {

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(metaClass);

        descriptors[0].getReadMethod()

        return null;
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(List<RecordRef> records,
                                             Map<String, String> attributes) {

        //impl

        return null;
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {
        return null;
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {
        return null;
    }

    @Override
    public ActionResults<RecordRef> executeAction(Collection<RecordRef> records,
                                                  GroupActionConfig processConfig) {
        return null;
    }

    @Override
    public Iterable<RecordRef> getIterableRecords(RecordsQuery query) {
        return null;
    }

    @Override
    public void register(RecordsDAO recordsSource) {

    }

    @Override
    public List<MetaAttributeDef> getAttributesDef(String sourceId, Collection<String> names) {
        return null;
    }

    @Override
    public Optional<MetaAttributeDef> getAttributeDef(String sourceId, String name) {
        return Optional.empty();
    }
}
