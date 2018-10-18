package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.records.*;
import ru.citeck.ecos.records.query.RecordsRefsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.remote.RestConnection;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

public class RemoteRecordsDAO extends AbstractRecordsDAO implements RecordsMetaDAO, RecordsActionExecutor {

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
    public RecordsRefsResult getRecords(RecordsQuery query) {

        RecordsPost.Request request = new RecordsPost.Request();

        if (enabled) {

            RecordRef afterId = query.getAfterId();
            request.query = new RecordsQuery(query);
            request.query.setSourceId(remoteSourceId);
            if (afterId != null) {
                request.query.setAfterId(new RecordRef(afterId.getId()));
            }

            RecordsRefsResult result = restConnection.jsonPost(recordsMethod, request, RecordsRefsResult.class);
            if (result != null) {
                return result.addSourceId(getId());
            } else {
                logger.error("[" + getId() + "] queryRecords will return nothing. " + request);
            }
        }
        return new RecordsRefsResult();
    }

    @Override
    public List<ObjectNode> getMeta(Collection<RecordRef> records, String gqlSchema) {
        List<String> recordsRefs = records.stream().map(RecordRef::getId).collect(Collectors.toList());
        String query = metaUtils.createQuery(metaBaseQuery, recordsRefs, gqlSchema);
        ExecutionResult executionResult = graphQLService.execute(restConnection, graphqlMethod, query, null);

        List<ObjectNode> meta = metaUtils.convertMeta(recordsRefs, executionResult);
        return RecordsUtils.convertToRefs(getId(), meta);
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
            ActionStatus status = ActionStatus.error(new RuntimeException("Remote action failed"));
            records.forEach(record -> results.getResults().add(new ActionResult<>(record, status)));
        }

        return results;
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
