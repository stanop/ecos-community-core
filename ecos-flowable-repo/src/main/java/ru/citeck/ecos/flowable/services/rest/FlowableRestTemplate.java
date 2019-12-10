package ru.citeck.ecos.flowable.services.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;
import ru.citeck.ecos.http.BasicAuthInterceptor;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class FlowableRestTemplate {

    private static final Log logger = LogFactory.getLog(RestFormService.class);

    @Value("${flowable.rest-api.username}")
    private String username;
    @Value("${flowable.rest-api.password}")
    private String password;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {

        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        converter.setObjectMapper(mapper);

        converters.add(converter);
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new SourceHttpMessageConverter<>());
        converters.add(new AllEncompassingFormHttpMessageConverter());

        HttpComponentsClientHttpRequestFactory factory = createHttpFactory();
        if (factory == null) {
            restTemplate = new RestTemplate(converters);
        } else {
            restTemplate = new RestTemplate(factory);
            restTemplate.setMessageConverters(converters);
        }

        restTemplate.getInterceptors().add(new BasicAuthInterceptor(username, password));
        restTemplate.getInterceptors().add(new FlowableAuthInterceptor());
    }

    public <T> ResponseEntity<T> exchange(String url,
                                          HttpMethod method,
                                          HttpEntity<?> requestEntity,
                                          Class<T> responseType,
                                          Object... uriVariables) {

        return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    public <T> T execute(String url,
                         HttpMethod method,
                         RequestCallback requestCallback,
                         ResponseExtractor<T> responseExtractor,
                         Object... uriVariables) {

        return restTemplate.execute(url, method, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T getForObject(String url, Class<T> clazz, Object... args) {
        return restTemplate.getForObject(url, clazz, args);
    }

    public <T> T getForObject(String url, Class<T> clazz, Map<String, ?> args) {
        return restTemplate.getForObject(url, clazz, args);
    }

    public <T> T getForObject(String url, Class<T> clazz) {
        return restTemplate.getForObject(url, clazz);
    }

    public <T> T postForObject(String url, Object request, Class<T> resp, Object... args) {
        return restTemplate.postForObject(url, request, resp, args);
    }

    public <T> T postForObject(String url, Object request, Class<T> resp, Map<String, ?> args) {
        return restTemplate.postForObject(url, request, resp, args);
    }

    public <T> T postForObject(String url, Object request, Class<T> resp) {
        return restTemplate.postForObject(url, request, resp);
    }

    private HttpComponentsClientHttpRequestFactory createHttpFactory() {
        /* Disable ssl verification */
        TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;

        SSLContext sslContext;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                                                        .loadTrustMaterial(null, acceptingTrustStrategy)
                                                        .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        CloseableHttpClient httpClient = HttpClients.custom()
                                                    .setSSLSocketFactory(csf)
                                                    .setSSLHostnameVerifier(hostnameVerifier)
                                                    .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }

    private class FlowableAuthInterceptor implements ClientHttpRequestInterceptor {

        private static final String TOKEN_COOKIE = "FLOWABLE_REMEMBER_ME";
        private final long TOKEN_AGE = TimeUnit.HOURS.toMillis(2);
        private final long TOKEN_ERROR_AGE = TimeUnit.MINUTES.toMillis(5);

        private String loginToken;
        private long tokenExpired;

        @Override
        public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
                                            ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {

            URI uri = httpRequest.getURI();

            String path = uri.getPath();
            if (path.contains("flowable-idm") || path.contains("flowable-rest")) {
                return clientHttpRequestExecution.execute(httpRequest, bytes);
            }

            String authToken = getLoginToken(uri.toURL());
            if (authToken != null) {
                String loginCookie = TOKEN_COOKIE + "=" + getLoginToken(uri.toURL());
                httpRequest.getHeaders().add("Cookie", loginCookie);
            }

            return clientHttpRequestExecution.execute(httpRequest, bytes);
        }

        private String getLoginToken(URL url) {

            if (System.currentTimeMillis() < tokenExpired) {
                return loginToken;
            }

            String authUrl = url.getProtocol() + "://" + url.getHost() +
                             ":" + url.getPort() + "/flowable-idm/app/authentication";

            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("j_username", username);
            params.set("j_password", password);
            params.set("_spring_security_remember_me", "true");
            params.set("submit", "Login");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);

            ResponseEntity<String> authEntity;
            try {
                authEntity = restTemplate.exchange(authUrl,
                                                   HttpMethod.POST,
                                                   requestEntity,
                                                   String.class);
            } catch (Exception e) {
                logger.warn("Flowable auth failed. Url: " + url + " AuthUrl: " + authUrl, e);
                tokenExpired = System.currentTimeMillis() + TOKEN_ERROR_AGE;
                return null;
            }

            List<String> cookieList = authEntity.getHeaders().get("Set-Cookie");
            if (cookieList == null || cookieList.isEmpty() || !cookieList.get(0).contains(TOKEN_COOKIE)) {
                logger.warn("Flowable auth failed. Url: " + url +
                            " AuthUrl: " + authUrl + " Entity: " + authEntity);
                tokenExpired = System.currentTimeMillis() + TOKEN_ERROR_AGE;
                return null;
            }

            String cookie = cookieList.get(0);

            loginToken = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
            tokenExpired = System.currentTimeMillis() + TOKEN_AGE;

            return loginToken;
        }
    }
}
