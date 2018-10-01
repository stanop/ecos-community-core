package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.JsonNode;
import graphql.ExecutionResult;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
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
public abstract class LocalRecordsDAO extends AbstractRecordsDAO {

    protected NodeService nodeService;
    protected GqlMetaUtils gqlMetaUtils;
    protected GraphQLService graphQLService;
    protected GroupActionService groupActionService;

    private String baseQuery;

    @PostConstruct
    public void init() {
        baseQuery = "records(refs:[\"%s\"])";
    }

    @Override
    public Map<RecordRef, JsonNode> getMeta(Collection<RecordRef> records, String gqlSchema) {
        List<String> recordsRefs = records.stream().map(Object::toString).collect(Collectors.toList());
        String query = gqlMetaUtils.createQuery(baseQuery, recordsRefs, gqlSchema);
        ExecutionResult executionResult = graphQLService.execute(query);
        return RecordsUtils.convertToRefs(gqlMetaUtils.convertMeta(recordsRefs, executionResult));
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
