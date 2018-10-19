package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.ExecutionResult;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pavel Simonov
 */
public abstract class LocalRecordsDAO extends AbstractRecordsDAO
                                      implements RecordsMetaDAO,
        RecordsActionExecutor {

    protected NodeService nodeService;
    protected GqlMetaUtils gqlMetaUtils;
    protected GraphQLService graphQLService;
    protected GroupActionService groupActionService;

    private boolean addSourceId = true;

    private String baseQuery;

    public LocalRecordsDAO() {
    }

    public LocalRecordsDAO(boolean addSourceId) {
        this.addSourceId = addSourceId;
    }

    @PostConstruct
    public void init() {
        baseQuery = "records(refs:[\"%s\"])";
    }

    @Override
    public List<ObjectNode> getMeta(Collection<RecordRef> records, String gqlSchema) {
        List<String> recordsRefs = records.stream().map(Object::toString).collect(Collectors.toList());
        String query = gqlMetaUtils.createQuery(baseQuery, recordsRefs, gqlSchema);
        ExecutionResult executionResult = graphQLService.execute(query);
        if (addSourceId) {
            return RecordsUtils.convertToRefs(getId(), gqlMetaUtils.convertMeta(recordsRefs, executionResult));
        } else {
            return gqlMetaUtils.convertMeta(recordsRefs, executionResult);
        }
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
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    public void setGqlMetaUtils(GqlMetaUtils gqlMetaUtils) {
        this.gqlMetaUtils = gqlMetaUtils;
    }

    @Autowired
    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }
}
