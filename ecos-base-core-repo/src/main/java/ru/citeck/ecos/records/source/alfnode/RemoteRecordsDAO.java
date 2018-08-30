package ru.citeck.ecos.records.source.alfnode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.social.support.URIBuilder;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLWebscript;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.AttributeInfo;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.source.RecordsDAO;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class RemoteRecordsDAO implements RecordsDAO {

    private static final String QUERY_RECORDS_METHOD = "citeck/ecos/records";
    private static final String GRAPHQL_METHOD = "citeck/ecos/graphql";

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    private String username;
    private String password;

    private String serverHost;
    private String sourceId;

    public RemoteRecordsDAO() {
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            this.restTemplate = new RestTemplate(createSecureTransport(username, password));
        } else {
            this.restTemplate = new RestTemplate();
        }
        StringHttpMessageConverter utfMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        restTemplate.getMessageConverters().add(utfMessageConverter);
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
        try {
            String postData = objectMapper.writeValueAsString(query);
            URI uri = URIBuilder.fromUri(serverHost + QUERY_RECORDS_METHOD).build();
            return postRequest(uri, postData, DaoRecordsResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, ObjectNode> queryMeta(Collection<String> records, String gqlSchema) {
        /*GraphQLWebscript.Request request = new GraphQLWebscript.Request();
        request.query = */

        /*try {
            String postData = prepareDataForGettingMetadata(gqlQuery, recordsResult);
            URI url = URIBuilder.fromUri(serverHost + REMOTE_GET_METADATA_METHOD).build();
            String responseDataString = postDataToRemote(url, postData);
            JournalData journalData = objectMapper.readValue(responseDataString, JournalData.class);
            return appendServerIdToRefs(journalData);
        } catch (IOException e) {
            logger.error(e);
        }
        return null;*/
        return null;
    }


    @Override
    public <V> Map<String, V> queryMeta(Collection<String> records, Class<V> metaClass) {
        return null;
    }

    @Override
    public Optional<AttributeInfo> getAttributeInfo(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String id) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public GroupAction<String> createAction(String actionId, GroupActionConfig config) {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    private <I, O> O postRequest(URI url, I postData, Class<O> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<I> requestEntity = new HttpEntity<>(postData, headers);
        return restTemplate.postForObject(url, requestEntity, responseType);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }


}
