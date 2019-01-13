package ru.citeck.ecos.records.source;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.meta.RecordsMetaService;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.request.result.RecordsResult;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public abstract class LocalRecordsDAO extends AbstractRecordsDAO implements RecordsActionExecutor {

    protected RecordsMetaService recordsMetaService;
    protected GroupActionService groupActionService;

    private boolean addSourceId = true;

    public LocalRecordsDAO() {
    }

    public LocalRecordsDAO(boolean addSourceId) {
        this.addSourceId = addSourceId;
    }

    public RecordsQueryResult<RecordMeta> getRecords(RecordsQuery query, String metaSchema) {

        if (this instanceof RecordsWithMetaDAO) {

            RecordsQueryResult<?> metaValues = getMetaValues(query);
            RecordsResult<RecordMeta> meta = recordsMetaService.getMeta(metaValues.getRecords(), metaSchema);
            if (addSourceId) {
                meta.setRecords(RecordsUtils.convertToRefs(getId(), meta.getRecords()));
            }

            RecordsQueryResult<RecordMeta> result = new RecordsQueryResult<>();
            result.merge(meta);
            result.setTotalCount(metaValues.getTotalCount());
            result.setHasMore(metaValues.getHasMore());

            return result;
        }

        throw new RuntimeException("RecordsDAO must implement RecordsWithMetaDAO");
    }

    public RecordsResult<RecordMeta> getMeta(List<RecordRef> records, String gqlSchema) {
        RecordsResult<RecordMeta> result = recordsMetaService.getMeta(getMetaValues(records), gqlSchema);
        if (addSourceId) {
            result.setRecords(RecordsUtils.toScopedRecordsMeta(getId(), result.getRecords()));
        }
        return result;
    }

    protected List<?> getMetaValues(List<RecordRef> records) {
        throw new RuntimeException("Not implemented");
    }

    protected RecordsQueryResult<?> getMetaValues(RecordsQuery query) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ActionResults<RecordRef> executeAction(List<RecordRef> records,
                                                  GroupActionConfig config) {
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
