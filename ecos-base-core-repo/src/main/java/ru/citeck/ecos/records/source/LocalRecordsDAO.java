package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.graphql.meta.GraphQLMetaService;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.result.RecordsResult;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public abstract class LocalRecordsDAO extends AbstractRecordsDAO implements RecordsActionExecutor {

    protected GraphQLMetaService graphQLMetaService;
    protected GroupActionService groupActionService;

    private boolean addSourceId = true;

    public LocalRecordsDAO() {
    }

    public LocalRecordsDAO(boolean addSourceId) {
        this.addSourceId = addSourceId;
    }

    public RecordsResult<ObjectNode> getRecords(RecordsQuery query, String metaSchema) {

        if (this instanceof RecordsWithMetaDAO) {

            RecordsResult<?> metaValues = getMetaValues(query);
            List<ObjectNode> meta = graphQLMetaService.getMeta(metaValues.getRecords(), metaSchema);
            meta = addSourceId ? RecordsUtils.convertToRefs(getId(), meta) : meta;

            RecordsResult<ObjectNode> result = new RecordsResult<>();
            result.setTotalCount(metaValues.getTotalCount());
            result.setHasMore(metaValues.getHasMore());
            result.setRecords(meta);

            return result;
        }

        throw new RuntimeException("RecordsDAO must implement RecordsWithMetaDAO");
    }

    public List<ObjectNode> getMeta(List<RecordRef> records, String gqlSchema) {
        List<ObjectNode> meta = graphQLMetaService.getMeta(getMetaValues(records), gqlSchema);
        return addSourceId ? RecordsUtils.convertToRefs(getId(), meta) : meta;
    }

    protected List<?> getMetaValues(List<RecordRef> records) {
        throw new RuntimeException("Not implemented");
    }

    protected RecordsResult<?> getMetaValues(RecordsQuery query) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ActionResults<RecordRef> executeAction(List<RecordRef> records, GroupActionConfig config) {
        return groupActionService.execute(records, config);
    }

    @Autowired
    public void setGroupActionService(GroupActionService groupActionService) {
        this.groupActionService = groupActionService;
    }

    @Autowired
    public void setGraphQLMetaService(GraphQLMetaService graphQLMetaService) {
        this.graphQLMetaService = graphQLMetaService;
    }
}
