package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsPost;
import ru.citeck.ecos.records.RecordsUtils;
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
    private ObjectMapper objectMapper;

    private String recordsMethod = "alfresco/service/citeck/ecos/records";
    private String graphqlMethod = "alfresco/service/citeck/ecos/graphql";

    private String remoteSourceId = "";

    public RemoteRecordsDAO() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
            try {

                RecordRef afterId = query.getAfterId();
                if (afterId != null) {
                    request.query = new RecordsQuery(query);
                    request.query.setAfterId(new RecordRef(afterId.getId()));
                } else {
                    request.query = query;
                }

                String postData = objectMapper.writeValueAsString(request);
                RecordsResult result = restConnection.jsonPost(recordsMethod, postData, RecordsResult.class);
                if (result != null) {
                    return result.addSourceId(getId());
                }
            } catch (JsonProcessingException e) {
                logger.error(e);
            }
        }
        logger.error("[" + getId() + "] queryRecords will return nothing. " + request);
        return new RecordsResult(query);
    }

    @Override
    public Map<RecordRef, JsonNode> queryMeta(Collection<RecordRef> records, String gqlSchema) {
        List<String> recordsRefs = records.stream().map(RecordRef::getId).collect(Collectors.toList());
        String query = metaUtils.createQuery(metaBaseQuery, recordsRefs, gqlSchema);
        ExecutionResult executionResult = graphQLService.execute(restConnection, graphqlMethod, query, null);
        return RecordsUtils.convertToRefs(getId(), metaUtils.convertMeta(recordsRefs, executionResult));
    }

    @Override
    public <V> Map<RecordRef, V> queryMeta(Collection<RecordRef> records, Class<V> metaClass) {
        List<String> recordsRefs = records.stream().map(RecordRef::getId).collect(Collectors.toList());
        String query = metaUtils.createQuery(metaBaseQuery, recordsRefs, metaClass);
        ExecutionResult executionResult = graphQLService.execute(restConnection, graphqlMethod, query, null);
        return RecordsUtils.convertToRefs(getId(), metaUtils.convertMeta(recordsRefs, executionResult, metaClass));
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String id) {
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

    public void setRestConnection(RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }
}
