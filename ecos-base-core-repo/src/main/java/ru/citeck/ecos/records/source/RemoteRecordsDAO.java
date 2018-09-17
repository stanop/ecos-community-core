package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.AttributeInfo;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.remote.RestConnection;

import javax.annotation.PostConstruct;
import java.util.*;

public class RemoteRecordsDAO extends AbstractRecordsDAO {

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
        metaBaseQuery = "records(source:\"" + remoteSourceId + "\",refs:[\"%s\"])";
    }

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public DaoRecordsResult queryRecords(RecordsQuery query) {
        if (!enabled) {
            return new DaoRecordsResult(query);
        }
        try {
            String postData = objectMapper.writeValueAsString(query);
            return restConnection.jsonPost(recordsMethod, postData, DaoRecordsResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, JsonNode> queryMeta(Collection<String> records, String gqlSchema) {
        List<String> recordsRefs = new ArrayList<>(records);
        String query = metaUtils.createQuery(metaBaseQuery, recordsRefs, gqlSchema);
        ExecutionResult executionResult = graphQLService.execute(restConnection, graphqlMethod, query, null);
        return metaUtils.convertMeta(recordsRefs, executionResult);
    }

    @Override
    public <V> Map<String, V> queryMeta(Collection<String> records, Class<V> metaClass) {
        List<String> recordsRefs = new ArrayList<>(records);
        String query = metaUtils.createQuery(metaBaseQuery, recordsRefs, metaClass);
        ExecutionResult executionResult = graphQLService.execute(restConnection, graphqlMethod, query, null);
        return metaUtils.convertMeta(recordsRefs, executionResult, metaClass);
    }

    @Override
    public Optional<AttributeInfo> getAttributeInfo(String name) {
        return Optional.empty();
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
