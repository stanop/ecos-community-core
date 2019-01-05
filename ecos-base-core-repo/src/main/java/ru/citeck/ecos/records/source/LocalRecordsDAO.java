package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.graphql.meta.GraphQLMetaService;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;

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

    public List<ObjectNode> getMeta(List<RecordRef> records, String gqlSchema) {
        List<ObjectNode> meta = graphQLMetaService.getMeta(getMetaValues(records), gqlSchema);
        return addSourceId ? RecordsUtils.convertToRefs(getId(), meta) : meta;
    }

    protected List<MetaValue> getMetaValues(List<RecordRef> records) {
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
