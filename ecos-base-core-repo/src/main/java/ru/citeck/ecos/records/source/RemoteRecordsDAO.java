package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.social.support.URIBuilder;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionPost;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.AttributeInfo;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RemoteRecordsDAO extends AbstractRecordsDAO {

    private static final String RECORDS_METHOD = "citeck/ecos/records";
    private static final String GRAPHQL_METHOD = "citeck/ecos/graphql";
    private static final String GROUP_ACTION_METHOD = "citeck/ecos/group-action";

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    private String username;

    private String password;
    private String serverHost;
    private String metaBaseQuery;

    private GraphQLService graphQLService;
    private GqlMetaUtils metaUtils;

    private boolean enabled = true;

    public RemoteRecordsDAO(String id) {
        super(id);
        metaBaseQuery = "records(source:\"\",refs:[\"%s\"])";
    }

    @PostConstruct
    public void init() {

        if (!enabled) {
            return;
        }

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            this.restTemplate = new RestTemplate(createSecureTransport(username, password));
        } else {
            this.restTemplate = new RestTemplate();
        }

        StringHttpMessageConverter utfMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        restTemplate.getMessageConverters().add(utfMessageConverter);

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private ClientHttpRequestFactory createSecureTransport(String username, String password) {
        HttpClient client = new HttpClient();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(AuthScope.ANY, credentials);
        return new CommonsClientHttpRequestFactory(client);
    }

    @Override
    public DaoRecordsResult queryRecords(RecordsQuery query) {
        if (!enabled) {
            return new DaoRecordsResult(query);
        }
        try {
            String postData = objectMapper.writeValueAsString(query);
            return postRequest(serverHost + RECORDS_METHOD, postData, DaoRecordsResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, JsonNode> queryMeta(Collection<String> records, String gqlSchema) {
        List<String> recordsRefs = new ArrayList<>(records);
        String query = metaUtils.createQuery(metaBaseQuery, recordsRefs, gqlSchema);
        ExecutionResult executionResult = graphQLService.execute(restTemplate,
                                                                serverHost + GRAPHQL_METHOD,
                                                                 query,
                                                                 null);
        return metaUtils.convertMeta(recordsRefs, executionResult);
    }

    @Override
    public <V> Map<String, V> queryMeta(Collection<String> records, Class<V> metaClass) {
        List<String> recordsRefs = new ArrayList<>(records);
        String query = metaUtils.createQuery(metaBaseQuery, recordsRefs, metaClass);
        ExecutionResult executionResult = graphQLService.execute(restTemplate,
                                                                 serverHost + GRAPHQL_METHOD,
                                                                 query,
                                                                 null);
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

    public <T> List<ActionResult<T>> execute(List<T> records, String actionId, GroupActionConfig config) {

        if (!enabled) {
            List<ActionResult<T>> results = new ArrayList<>();
            records.forEach(r -> results.add(new ActionResult<>(r, ActionStatus.STATUS_SKIPPED)));
            return results;
        }

        GroupActionPost.ActionData actionData = new GroupActionPost.ActionData();
        actionData.nodes = records;
        actionData.actionId = actionId;
        actionData.config = config;

        String uri = serverHost + GROUP_ACTION_METHOD;
        GroupActionPost.Response<T> response = postRequest(uri, actionData, GroupActionPost.Response.class);

        return response.results;
    }

    private <I, O> O postRequest(String strUrl, I postData, Class<O> responseType) {
        URI uri = URIBuilder.fromUri(strUrl).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<I> requestEntity = new HttpEntity<>(postData, headers);
        return restTemplate.postForObject(uri, requestEntity, responseType);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
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

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
