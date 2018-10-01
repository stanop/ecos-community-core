package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.JsonNode;
import graphql.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.*;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.remote.RestConnection;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

public class RemoteRecordsDAO extends AbstractRecordsDAO {

    private static final Log logger = LogFactory.getLog(RemoteRecordsDAO.class);

    private String metaBaseQuery;

    private GraphQLService graphQLService;
    private GqlMetaUtils metaUtils;

    private boolean enabled = true;

    private RestConnection restConnection;

    private String recordsMethod = "alfresco/service/citeck/ecos/records";
    private String graphqlMethod = "alfresco/service/citeck/ecos/graphql";
    private String groupActionMethod = "alfresco/service/citeck/ecos/records-group-action";

    private String remoteSourceId = "";

    public RemoteRecordsDAO() {
    }

    @PostConstruct
    public void init() {
        metaBaseQuery = "records(refs:[\"%s\"])";
    }

    @Override
    public RecordsResult queryRecords(RecordsQuery query) {

        RecordsPost.Request request = new RecordsPost.Request();
        request.sourceId = remoteSourceId;

        if (enabled) {

            RecordRef afterId = query.getAfterId();
            if (afterId != null) {
                request.query = new RecordsQuery(query);
                request.query.setAfterId(new RecordRef(afterId.getId()));
            } else {
                request.query = query;
            }

            RecordsResult result = restConnection.jsonPost(recordsMethod, request, RecordsResult.class);
            if (result != null) {
                return result.addSourceId(getId());
            } else {
                logger.error("[" + getId() + "] queryRecords will return nothing. " + request);
            }
        }
        return new RecordsResult(query);
    }

    @Override
    public Map<RecordRef, JsonNode> getMeta(Collection<RecordRef> records, String gqlSchema) {
        List<String> recordsRefs = records.stream().map(RecordRef::getId).collect(Collectors.toList());
        String query = metaUtils.createQuery(metaBaseQuery, recordsRefs, gqlSchema);
        ExecutionResult executionResult = graphQLService.execute(restConnection, graphqlMethod, query, null);
        return RecordsUtils.convertToRefs(getId(), metaUtils.convertMeta(recordsRefs, executionResult));
    }

    @Override
    public ActionResults<RecordRef> executeAction(List<RecordRef> records, GroupActionConfig config) {

        RecordsGroupActionPost.ActionData data = new RecordsGroupActionPost.ActionData();
        data.config = config;
        data.nodes = RecordsUtils.toLocalRecords(records);

        RecordsGroupActionPost.Response response =
                restConnection.jsonPost(groupActionMethod, data, RecordsGroupActionPost.Response.class);

        ActionResults<RecordRef> results;

        if (response != null) {
            results = new ActionResults<>(response.results, r -> new RecordRef(getId(), r));
        } else {
            results = new ActionResults<>();
            ActionStatus status = new ActionStatus(ActionStatus.STATUS_ERROR);
            status.setMessage("Remote action failed");
            records.forEach(record -> results.getResults().add(new ActionResult<>(record, status)));
        }

        return results;
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, RecordRef recordRef) {
        throw new RuntimeException("getMetaValue is not supported for remote recordsDAO");
    }

    public void setRemoteSourceId(String remoteSourceId) {
        this.remoteSourceId = remoteSourceId;
    }

    public void setRecordsMethod(String recordsMethod) {
        this.recordsMethod = recordsMethod;
    }

    public void setGraphqlMethod(String graphqlMethod) {
        this.graphqlMethod = graphqlMethod;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Autowired
    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }

    @Autowired
    public void setMetaUtils(GqlMetaUtils metaUtils) {
        this.metaUtils = metaUtils;
    }

    public void setGroupActionMethod(String groupActionMethod) {
        this.groupActionMethod = groupActionMethod;
    }

    public void setRestConnection(RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }
}
