package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.converter.ConvertersProvider;
import ru.citeck.ecos.graphql.meta.converter.MetaAtt;
import ru.citeck.ecos.graphql.meta.converter.MetaConverter;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.actions.RecordsActionFactory;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.source.RecordsDAO;

import java.util.Collection;
import java.util.List;

/**
 * Service to work with some abstract "records" from any source
 * It may be alfresco nodes, database records, generated data and so on
 * A record id contains two parts: 'sourceId' and 'id'. String representation: sourceId@id
 *
 * @see RecordRef
 * @see RecordsDAO
 */
public interface RecordsService {

    /**
     * Query records from default RecordsDAO
     * @return list of RecordRef and page info
     */
    RecordsResult<RecordRef> getRecords(RecordsQuery query);

    /**
     * Query records with meta
     * @param metaSchema GraphQL schema for MetaValue
     */
    RecordsResult<ObjectNode> getRecords(RecordsQuery query, String metaSchema);

    /**
     * Query records with meta
     * @param metaClass POJO to generate metadata GQL schema and retrieve data
     *                  This class must contain constructor without arguments and have public fields
     *                  Getters/setters is not yet supported
     */
    <T> RecordsResult<T> getRecords(RecordsQuery query, Class<T> metaClass);

    /**
     * Get metadata for specified records
     *
     * @param metaSchema GraphQL schema for MetaValue
     * @see MetaValue
     */
    List<ObjectNode> getMeta(Collection<RecordRef> records, String metaSchema);

    /**
     * Get metadata for specified records.
     * @param metaClass POJO to generate metadata GQL schema and retrieve data
     *                  This class must contain constructor without arguments and have public fields
     *                  Getters/setters is not yet supported
     *
     * @see ConvertersProvider
     * @see MetaConverter
     * @see MetaAtt
     */
    <T> List<T> getMeta(Collection<RecordRef> records, Class<T> metaClass);

    /**
     * Get MetaValue by record. Executed in GraphQL execution context
     */
    List<MetaValue> getMetaValues(GqlContext context, List<RecordRef> records);

    /**
     * Execute action with specified records.
     * Action with every record can be executed in different ways depends on RecordsDAO implementation
     * You can use RecordsActionFactory to combine remote/local records processing
     * This method used only for process finite amount of records.
     * To process many records you can use combination of getIterableRecords and GroupActionService.execute
     *
     * @see RecordsActionFactory
     * @see GroupActionService
     */
    ActionResults<RecordRef> executeAction(Collection<RecordRef> records, GroupActionConfig processConfig);

    /**
     * Get Iterable with records which fit the query from default source.
     * This method can be used to process all records in system without search limits
     */
    Iterable<RecordRef> getIterableRecords(RecordsQuery query);

    /**
     * Register new RecordsDAO. It must return valid id from method "getId()" to call this method.
     */
    void register(RecordsDAO recordsSource);
}
