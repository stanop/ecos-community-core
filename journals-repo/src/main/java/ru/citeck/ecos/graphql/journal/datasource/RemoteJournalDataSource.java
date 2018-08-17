package ru.citeck.ecos.graphql.journal.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.social.support.URIBuilder;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GqlExecutionResult;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.records.RecordsResult;
import ru.citeck.ecos.repo.RemoteRef;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RemoteJournalDataSource implements JournalDataSource {

    private static Log logger = LogFactory.getLog(RemoteJournalDataSource.class);

    private static final String REMOTE_GET_ID_METHOD = "/ecos/journals/remote/getId";
    private static final String REMOTE_GET_METADATA_METHOD = "/ecos/journals/remote/getMetadata";

    private static final String DEFAULT_REMOTE_DATASOURCE = "ecos.journals.datasource.AlfNodes";

    private String username;
    private String password;
    private String serverHost;
    private String serverId;
    private String remoteDataSourceBeanName;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void init() {
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
    public JGqlRecordsConnection getRecords(GqlContext context,
                                            String query,
                                            String language,
                                            JGqlPageInfoInput pageInfo) {
        return null;
    }

    @Override
    public RecordsResult queryIds(GqlContext context,
                                  String query,
                                  String language,
                                  JGqlPageInfoInput pageInfo) {
        try {
            String postData = prepareDataForGettingIds(query, language, pageInfo);
            URI url = URIBuilder.fromUri(serverHost + REMOTE_GET_ID_METHOD).build();
            String responseDataString = postDataToRemote(url, postData);
            RecordsResult responseData = objectMapper.readValue(responseDataString, RecordsResult.class);
            return appendServerIdToRefs(responseData);
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public List<JGqlAttributeValue> convertToGqlValue(GqlContext context,
                                                      List<RemoteRef> remoteRefList) {
        return null;
    }

    @Override
    public ExecutionResult queryMetadata(JournalType journalType,
                                         String gqlQuery,
                                         List<RemoteRef> remoteRefList) {
        try {
            String postData = prepareDataForGettingMetadata(gqlQuery, remoteRefList);
            URI url = URIBuilder.fromUri(serverHost + REMOTE_GET_METADATA_METHOD).build();
            String responseDataString = postDataToRemote(url, postData);
            ExecutionResult responseData = objectMapper.readValue(responseDataString, GqlExecutionResult.class); //TODO указать в кого конкретно сериализовать.
            return appendServerIdToRefs(responseData);
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName) {
        return Optional.empty();
    }

    @Override
    public boolean isSupportsSplitLoading() {
        return true;
    }

    private RecordsResult appendServerIdToRefs(RecordsResult prev) {
        if (prev == null || prev.records == null) {
            return prev;
        }

        List<RemoteRef> remoteRefList = new ArrayList<>(prev.records.size());
        prev.records.forEach(remoteRef -> {
            if (remoteRef.isRemote()) {
                remoteRefList.add(remoteRef);
            } else {
                remoteRefList.add(new RemoteRef(serverId, remoteRef.toString()));
            }
        });
        return new RecordsResult(remoteRefList, prev.hasNext, prev.totalCount, prev.skipCount, prev.maxItems);
    }

    private String prepareDataForGettingIds(String query, String language, JGqlPageInfoInput pageInfo)
            throws JsonProcessingException {
        GetIdsRequest requestData = new GetIdsRequest(query, language, pageInfo);
        if (StringUtils.isNotBlank(remoteDataSourceBeanName)) {
            requestData.datasource = remoteDataSourceBeanName;
        } else {
            requestData.datasource = DEFAULT_REMOTE_DATASOURCE;
        }
        return objectMapper.writeValueAsString(requestData);
    }

    private ExecutionResult appendServerIdToRefs(ExecutionResult responseData) {
        return responseData; //TODO: сделать добавление id сервера к нодам, когда буду знать структуру того, что сюда приходит.
    }

    private String prepareDataForGettingMetadata(String gqlQuery,
                                                 List<RemoteRef> remoteRefList) throws JsonProcessingException {
        GetMetadataRequest requestData = new GetMetadataRequest();
        requestData.remoteRefs = remoteRefList;
        requestData.gqlQuery = gqlQuery;
        if (StringUtils.isNotBlank(remoteDataSourceBeanName)) {
            requestData.datasource = remoteDataSourceBeanName;
        } else {
            requestData.datasource = DEFAULT_REMOTE_DATASOURCE;
        }
        return objectMapper.writeValueAsString(requestData);
    }

    private String postDataToRemote(URI url, String postData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(postData, headers);
        return restTemplate.postForObject(url, requestEntity, String.class);
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

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void setRemoteDataSourceBeanName(String remoteDataSourceBeanName) {
        this.remoteDataSourceBeanName = remoteDataSourceBeanName;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class GetIdsRequest {
        public String query;
        public String language;
        public JGqlPageInfoInput jGqlPageInfoInput;
        public String datasource;

        public GetIdsRequest() {
        }

        GetIdsRequest(String query, String language, JGqlPageInfoInput jGqlPageInfoInput) {
            this.query = query;
            this.language = language;
            this.jGqlPageInfoInput = jGqlPageInfoInput;
        }
    }

    private static class GetMetadataRequest {
        public String datasource;
        public String gqlQuery;
        public List<RemoteRef> remoteRefs;
    }

}
