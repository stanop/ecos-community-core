package ru.citeck.ecos.http;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class BasicAuthInterceptor implements ClientHttpRequestInterceptor {

    private static final String HEADER_AUTHORIZATION = "Authorization";

    private String username;
    private String password;

    public BasicAuthInterceptor(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
                                        ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {

        HttpHeaders headers = httpRequest.getHeaders();
        headers.add(HEADER_AUTHORIZATION, encodeCredentialsForBasicAuth(username, password));
        return clientHttpRequestExecution.execute(httpRequest, bytes);
    }

    public static String encodeCredentialsForBasicAuth(String username, String password) {
        return "Basic " + new Base64().encodeToString((username + ":" + password).getBytes());
    }
}
