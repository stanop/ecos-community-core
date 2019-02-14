package ru.citeck.ecos.records.source.dao.local;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.meta.RecordsMetaService;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.request.result.RecordsResult;
import ru.citeck.ecos.records.source.dao.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Local records DAO
 *
 * Extend this DAO if your data located in the same alfresco instance (when you don't want to execute graphql query remotely)
 * Important: This class implement only RecordsDAO. All other interfaces should be implemented in children classes
 *
 * @see RecordsQueryDAO
 * @see RecordsMetaDAO
 * @see RecordsQueryWithMetaDAO
 * @see RecordsActionExecutor
 * @see MutableRecordsDAO
 *
 * @author Pavel Simonov
 */
public abstract class LocalRecordsDAO extends AbstractRecordsDAO {

    private static final Log logger = LogFactory.getLog(LocalRecordsDAO.class);

    protected RecordsMetaService recordsMetaService;
    protected GroupActionService groupActionService;

    protected ObjectMapper objectMapper = new ObjectMapper();

    private boolean addSourceId = true;

    {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public LocalRecordsDAO() {
    }

    public LocalRecordsDAO(boolean addSourceId) {
        this.addSourceId = addSourceId;
    }

    public RecordsMutResult mutate(RecordsMutation mutation) {

        List<RecordRef> recordRefs = mutation.getRecords()
                                             .stream()
                                             .map(RecordMeta::getId)
                                             .collect(Collectors.toList());

        if (this instanceof MutableRecordsLocalDAO) {

            MutableRecordsLocalDAO mutableDao = (MutableRecordsLocalDAO) this;
            List<?> values = mutableDao.getValuesToMutate(recordRefs);

            for (int i = 0; i < recordRefs.size(); i++) {

                RecordMeta meta = mutation.getRecords().get(i);

                try {
                    objectMapper.readerForUpdating(values.get(i)).readValue(meta.getAttributes());
                } catch (IOException e) {
                    throw new RuntimeException("Mutation failed", e);
                }
            }

            return mutableDao.save(values);
        }

        logger.warn("[" + getId() + "] RecordsDAO doesn't implement MutableRecordsLocalDAO");

        return new RecordsMutResult();
    }

    public RecordsQueryResult<RecordMeta> getRecords(RecordsQuery query, String metaSchema) {

        RecordsQueryResult<RecordMeta> queryResult = new RecordsQueryResult<>();

        List<RecordRef> recordRefs = new ArrayList<>();
        List<Object> rawMetaValues = new ArrayList<>();

        if (this instanceof RecordsQueryWithMetaLocalDAO) {

            RecordsQueryWithMetaLocalDAO withMeta = (RecordsQueryWithMetaLocalDAO) this;
            RecordsQueryResult<?> values = withMeta.getMetaValues(query);

            queryResult.setTotalCount(values.getTotalCount());
            queryResult.setHasMore(values.getHasMore());

            for (Object record : values.getRecords()) {
                if (record instanceof RecordRef) {
                    recordRefs.add((RecordRef) record);
                } else {
                    rawMetaValues.add(record);
                }
            }

        } else if (this instanceof RecordsQueryDAO) {

            RecordsQueryDAO recordsQueryDAO = (RecordsQueryDAO) this;
            RecordsQueryResult<RecordRef> records = recordsQueryDAO.getRecords(query);
            queryResult.merge(records);
            queryResult.setHasMore(records.getHasMore());
            queryResult.setTotalCount(records.getTotalCount());

            recordRefs.addAll(records.getRecords());
        }

        if (!recordRefs.isEmpty()) {
            if (this instanceof RecordsMetaLocalDAO) {
                RecordsMetaLocalDAO metaDao = (RecordsMetaLocalDAO) this;
                rawMetaValues.addAll(metaDao.getMetaValues(recordRefs));
            } if (this instanceof RecordsMetaDAO) {
                RecordsMetaDAO metaDao = (RecordsMetaDAO) this;
                RecordsResult<RecordMeta> meta = metaDao.getMeta(recordRefs, metaSchema);
                queryResult.merge(meta);
            } else {
                logger.warn("[" + getId() + "] RecordsDAO implements neither " +
                            "RecordsMetaLocalDAO nor RecordsMetaDAO. We can't receive metadata");
                recordRefs.stream().map(RecordMeta::new).forEach(queryResult::addRecord);
            }
        }

        if (!rawMetaValues.isEmpty()) {
            queryResult.merge(recordsMetaService.getMeta(rawMetaValues, metaSchema));
        }

        if (addSourceId) {
            queryResult.setRecords(RecordsUtils.convertToRefs(getId(), queryResult.getRecords()));
        }

        return queryResult;
    }

    public RecordsResult<RecordMeta> getMeta(List<RecordRef> records, String metaSchema) {

        RecordsResult<RecordMeta> result;

        if (this instanceof RecordsMetaLocalDAO) {

            RecordsMetaLocalDAO metaLocalDao = (RecordsMetaLocalDAO) this;
            List<?> metaValues = metaLocalDao.getMetaValues(addSourceId ?
                    RecordsUtils.toLocalRecords(records) : records);
            result = recordsMetaService.getMeta(metaValues, metaSchema);

        } else {

            logger.warn("[" + getId() + "] RecordsDAO doesn't implement " +
                        "RecordsMetaLocalDAO. We can't receive metadata");

            result = new RecordsResult<>();
            records.stream().map(RecordMeta::new).forEach(result::addRecord);
        }

        if (addSourceId) {
            result.setRecords(RecordsUtils.convertToRefs(getId(), result.getRecords()));
        }

        return result;
    }

    public ActionResults<RecordRef> executeAction(List<RecordRef> records, GroupActionConfig config) {
        return groupActionService.execute(records, config);
    }

    @Autowired
    public void setGroupActionService(GroupActionService groupActionService) {
        this.groupActionService = groupActionService;
    }

    @Autowired
    public void setRecordsMetaService(RecordsMetaService recordsMetaService) {
        this.recordsMetaService = recordsMetaService;
    }
}
