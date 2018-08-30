package ru.citeck.ecos.graphql.journal.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.graphql.journal.response.JournalData;
import ru.citeck.ecos.graphql.journal.response.converter.impl.SplitLoadingResponseConverter;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.journals.records.JournalRecordsResult;
import ru.citeck.ecos.providers.ApplicationContextProvider;
import ru.citeck.ecos.records.RecordRef;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RemoteJournalDataSource implements JournalDataSource {

    private static Log logger = LogFactory.getLog(RemoteJournalDataSource.class);

    private static final String REMOTE_GET_ID_METHOD = "/ecos/journals/getId";
    private static final String REMOTE_GET_METADATA_METHOD = "/ecos/journals/getMetadata";
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
    public String getServerId() {
        return serverId;
    }

    @Override
    public JournalRecordsResult queryIds(GqlContext context,
                                         String query,
                                         String language,
                                         JGqlPageInfoInput pageInfo) {
        try {
            String postData = prepareDataForGettingIds(query, language, pageInfo);
            URI url = URIBuilder.fromUri(serverHost + REMOTE_GET_ID_METHOD).build();
            String responseDataString = postDataToRemote(url, postData);
            JournalRecordsResult responseData = objectMapper.readValue(responseDataString, JournalRecordsResult.class);
            return responseData;
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public List<MetaValue> convertToGqlValue(GqlContext context,
                                             List<RecordRef> remoteRefList) {
        return null;
    }

    @Override
    public JournalData queryMetadata(String gqlQuery,
                                     String dataSourceBeanName,
                                     JournalRecordsResult recordsResult) {
        try {
            String postData = prepareDataForGettingMetadata(gqlQuery, recordsResult);
            URI url = URIBuilder.fromUri(serverHost + REMOTE_GET_METADATA_METHOD).build();
            String responseDataString = postDataToRemote(url, postData);
            JournalData journalData = objectMapper.readValue(responseDataString, JournalData.class);
            return appendServerIdToRefs(journalData);
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName) {
        String beanName = getRemoteDataSourceBeanName();
        JournalDataSource datasource = (JournalDataSource) ApplicationContextProvider.getBean(beanName);
        return datasource.getAttributeInfo(attributeName);
    }

    @Override
    public List<String> getDefaultAttributes() {
        String beanName = getRemoteDataSourceBeanName();
        JournalDataSource datasource = (JournalDataSource) ApplicationContextProvider.getBean(beanName);
        return datasource.getDefaultAttributes();
    }

    @Override
    public boolean isSupportsSplitLoading() {
        String beanName = getRemoteDataSourceBeanName();
        JournalDataSource datasource = (JournalDataSource) ApplicationContextProvider.getBean(beanName);
        return datasource.isSupportsSplitLoading();
    }

    /*private JournalRecordsResult appendServerIdToRefs(JournalRecordsResult prev) {
        if (prev == null || prev.records == null) {
            return prev;
        }

        List<RecordRef> remoteRefList = new ArrayList<>(prev.records.size());
        prev.records.forEach(remoteRef -> {
            if (remoteRef.isRemote()) {
                remoteRefList.add(remoteRef);
            } else {
                remoteRefList.add(new RecordRef(serverId, remoteRef.toString()));
            }
        });
        return new JournalRecordsResult(remoteRefList, prev.hasNext, prev.totalCount, prev.skipCount, prev.maxItems);
    }*/

    private String prepareDataForGettingIds(String query, String language, JGqlPageInfoInput pageInfo)
            throws JsonProcessingException {
        GetIdsRequest requestData = new GetIdsRequest(query, language, pageInfo);
        requestData.datasource = getRemoteDataSourceBeanName();
        return objectMapper.writeValueAsString(requestData);
    }

    private JournalData appendServerIdToRefs(JournalData journalData) {
        /*List<LinkedHashMap> records = journalData.getData().getJournalRecords().getRecords();
        for (LinkedHashMap map : records) {
            String id = (String) map.get("id");
            if (id != null) {
                RecordRef remoteRef = new RecordRef(serverId, id);
                map.put("id", remoteRef.toString());
            }
        }*/
        return journalData;
    }

    private String prepareDataForGettingMetadata(String gqlQuery, JournalRecordsResult recordsResult)
            throws JsonProcessingException {
        GetMetadataRequest requestData = new GetMetadataRequest();
        requestData.remoteRefs = recordsResult.records;
        requestData.gqlQuery = gqlQuery;
        requestData.datasource = getRemoteDataSourceBeanName();

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(SplitLoadingResponseConverter.PAGINATION_TOTAL_COUNT_KEY, recordsResult.totalCount);
        additionalData.put(SplitLoadingResponseConverter.PAGINATION_SKIP_COUNT_KEY, recordsResult.skipCount);
        additionalData.put(SplitLoadingResponseConverter.PAGINATION_MAX_ITEMS_KEY, recordsResult.maxItems);
        additionalData.put(SplitLoadingResponseConverter.PAGINATION_HAS_NEXT_PAGE_KEY, recordsResult.hasNext);
        requestData.additionalData = additionalData;

        return objectMapper.writeValueAsString(requestData);
    }

    private String postDataToRemote(URI url, String postData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(postData, headers);
        return restTemplate.postForObject(url, requestEntity, String.class);
    }

    public String getRemoteDataSourceBeanName() {
        if (StringUtils.isNotBlank(remoteDataSourceBeanName)) {
            return remoteDataSourceBeanName;
        } else {
            return DEFAULT_REMOTE_DATASOURCE;
        }
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
        public List<RecordRef> remoteRefs;
        public Map<String, Object> additionalData;
    }

}
