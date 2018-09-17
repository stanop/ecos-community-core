package ru.citeck.ecos.remote;

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

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class RestConnection {

    private static final Log logger = LogFactory.getLog(RestConnection.class);

    private RestTemplate restTemplate;

    private String username;
    private String password;

    private String host;

    private boolean enabled = false;
    private boolean initialized = false;

    @PostConstruct
    public synchronized void init() {

        if (initialized || !enabled) {
            return;
        }

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            this.restTemplate = new RestTemplate(createSecureTransport(username, password));
        } else {
            this.restTemplate = new RestTemplate();
        }

        StringHttpMessageConverter utfMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        restTemplate.getMessageConverters().add(utfMessageConverter);

        initialized = true;
    }

    private ClientHttpRequestFactory createSecureTransport(String username, String password) {
        HttpClient client = new HttpClient();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(AuthScope.ANY, credentials);
        return new CommonsClientHttpRequestFactory(client);
    }

    public <I, O> O jsonPost(String strUrl, I postData, Class<O> responseType) {
        if (!initialized || !enabled) {
            logger.warn("Rest connection is not initialized or disabled! " +
                        "Return null for query " + strUrl + " with data: " + postData + ". " +
                        "Host: " + host);
            return null;
        }
        String hostUrl;
        if (!strUrl.startsWith("/")) {
            hostUrl = host + "/" + strUrl;
        } else {
            hostUrl = host + strUrl;
        }
        URI uri = URIBuilder.fromUri(hostUrl).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<I> requestEntity = new HttpEntity<>(postData, headers);
        return restTemplate.postForObject(uri, requestEntity, responseType);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            init();
        }
    }
}
