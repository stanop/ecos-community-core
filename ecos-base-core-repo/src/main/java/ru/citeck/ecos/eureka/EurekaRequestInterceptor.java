package ru.citeck.ecos.eureka;

import com.netflix.appinfo.InstanceInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class EurekaRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Log logger = LogFactory.getLog(EurekaRequestInterceptor.class);

    private EcosEurekaClient client;

    public EurekaRequestInterceptor(EcosEurekaClient client) {
        this.client = client;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        return execution.execute(new ResolvedRequest(request), body);
    }

    class ResolvedRequest implements HttpRequest {

        private HttpRequest original;

        ResolvedRequest(HttpRequest original) {
            this.original = original;
        }

        @Override
        public HttpMethod getMethod() {
            return original.getMethod();
        }

        @Override
        public URI getURI() {

            URI uri = original.getURI();
            String host = uri.getHost();

            if (StringUtils.isBlank(host) || uri.getPort() != -1) {
                return uri;
            }

            InstanceInfo info = null;
            try {
                info = client.getInstanceInfo(host);
            } catch (Exception e) {
                logger.warn("Host can't be resolved: " + host, e);
            }
            if (info == null) {
                return uri;
            }

            String resolvedHost = info.getHostName() + ":" + info.getPort();
            String newUriStr = uri.toString().replace("://" + host, "://" + resolvedHost);

            try {
                return new URI(newUriStr);
            } catch (URISyntaxException e) {
                logger.error("URI can't be created. Value: " + newUriStr, e);
                return uri;
            }
        }

        @Override
        public HttpHeaders getHeaders() {
            return original.getHeaders();
        }
    }
}
