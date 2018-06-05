package ru.citeck.ecos.flowable.services.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.flowable.form.model.SimpleFormModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.http.BasicAuthInterceptor;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RestFormService {

    private static final String FLOWABLE_REST_API_KEY = "${flowable.rest-api.url}";

    private final static Log logger = LogFactory.getLog(RestFormService.class);

    @Value("${flowable.rest-api.username}")
    private String username;
    @Value("${flowable.rest-api.password}")
    private String password;
    @Value(FLOWABLE_REST_API_KEY)
    private String restApiUrl;

    @Value("${flowable.rest-api.form.form-definitions-by-key}")
    private String formDefinitionsByKeyUrl;
    @Value("${flowable.rest-api.form.form-model}")
    private String formModelUrl;

    private String baseUrl;
    private RestTemplate restTemplate;

    private boolean initialized = false;

    @PostConstruct
    public void init() {

        if (FLOWABLE_REST_API_KEY.equals(restApiUrl)) {
            logger.error("Flowable rest api url is not defined!");
            return;
        }

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        converter.setObjectMapper(mapper);

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(converter);

        HttpComponentsClientHttpRequestFactory factory = createHttpFactory();
        if (factory == null) {
            restTemplate = new RestTemplate(converters);
        } else {
            restTemplate = new RestTemplate(factory);
            restTemplate.setMessageConverters(converters);
        }

        restTemplate.getInterceptors().add(new BasicAuthInterceptor(username, password));

        baseUrl = restApiUrl.endsWith("/") ? restApiUrl : restApiUrl + "/";
        baseUrl += "form-api/form-repository/";

        initialized = true;
    }

    private HttpComponentsClientHttpRequestFactory createHttpFactory() {
        /** Disable ssl verification */
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (KeyManagementException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (KeyStoreException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }

    public Optional<SimpleFormModel> getFormByKey(String formKey) {

        if (!initialized) {
            logger.warn("Flowable rest form service is not initialized! FormKey: " + formKey);
            return Optional.empty();
        }

        String url = baseUrl + formDefinitionsByKeyUrl;
        FormDefinitions definitions = restTemplate.getForObject(url, FormDefinitions.class, formKey);

        if (definitions.data != null && definitions.data.size() > 0) {

            String formDefinitionId = definitions.data.get(0).id;
            url = baseUrl + formModelUrl;

            return Optional.ofNullable(restTemplate.getForObject(url, SimpleFormModel.class, formDefinitionId));
        }

        return Optional.empty();
    }

    public boolean hasFormWithKey(String formKey) {
        if (initialized) {
            String url = baseUrl + formDefinitionsByKeyUrl;
            FormDefinitions definitions = restTemplate.getForObject(url, FormDefinitions.class, formKey);
            return definitions.data != null && definitions.data.size() > 0;
        }
        return false;
    }

    private static class FormDefinitions {

        public Integer total;
        public Integer start;
        public String sort;
        public String order;
        public Integer size;
        public List<Data> data;

        public static class Data {
            public String id;
            public String url;
            public String category;
            public String name;
            public String key;
            public String description;
            public Integer version;
            public String resourceName;
            public String deploymentId;
            public String parentDeploymentId;
            public String tenantId;
        }
    }
}
