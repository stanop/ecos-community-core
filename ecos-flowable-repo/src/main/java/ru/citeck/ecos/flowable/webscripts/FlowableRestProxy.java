package ru.citeck.ecos.flowable.webscripts;

import org.alfresco.repo.admin.SysAdminParams;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.webscripts.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import ru.citeck.ecos.flowable.services.rest.FlowableRestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class FlowableRestProxy extends AbstractWebScript {

    private static final String WEBSCRIPT_URL = "citeck/flowable/proxy/";
    private static final String FLOWABLE_HOST_KEY = "${flowable.host.url}";

    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    private static final String[] HEADERS_TO = {
            HttpHeaders.ACCEPT,
            HttpHeaders.ACCEPT_CHARSET,
            HttpHeaders.ACCEPT_ENCODING,
            HttpHeaders.ACCEPT_LANGUAGE,
            HttpHeaders.ACCEPT_RANGES,
            HttpHeaders.AGE,
            HttpHeaders.ALLOW,
            HttpHeaders.CACHE_CONTROL,
            HttpHeaders.CONNECTION,
            HttpHeaders.CONTENT_ENCODING,
            HttpHeaders.CONTENT_LANGUAGE,
            HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.CONTENT_LOCATION,
            HttpHeaders.CONTENT_RANGE,
            HttpHeaders.EXPECT
    };

    private static final String[] HEADERS_FROM = {
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.CONTENT_ENCODING,
            HEADER_CONTENT_DISPOSITION,
            HttpHeaders.CACHE_CONTROL,
            HttpHeaders.PRAGMA
    };

    private static final Map<String, BiConsumer<WebScriptResponse, String>> HEADERS_FROM_CONSUMER;

    private static final Log logger = LogFactory.getLog(FlowableRestProxy.class);

    static {
        Map<String, BiConsumer<WebScriptResponse, String>> headersConsumers = new HashMap<>();
        headersConsumers.put(HttpHeaders.CONTENT_TYPE, WebScriptResponse::setContentType);
        headersConsumers.put(HttpHeaders.CONTENT_ENCODING, WebScriptResponse::setContentEncoding);
        HEADERS_FROM_CONSUMER = Collections.unmodifiableMap(headersConsumers);
    }

    @Value(FLOWABLE_HOST_KEY)
    private String flowableHost;

    @Autowired
    private FlowableRestTemplate restTemplate;
    @Autowired
    @Qualifier("sysAdminParams")
    private SysAdminParams sysAdminParams;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) {

        if (flowableHost == null || flowableHost.isEmpty() || FLOWABLE_HOST_KEY.equals(flowableHost)) {
            flowableHost = sysAdminParams.getAlfrescoProtocol() + "://" +
                           sysAdminParams.getAlfrescoHost() + ":" +
                           sysAdminParams.getAlfrescoPort();
        }

        String desc = getDescription().getId();
        String ext = FilenameUtils.getExtension(desc);
        HttpMethod method = HttpMethod.valueOf(ext.toUpperCase());

        String url = getFlowableUrl(req.getURL());
        res.setContentEncoding("UTF-8");

        try {
            restTemplate.execute(url, method,
                request -> prepareRequest(req, request),
                response -> processResponse(res, response)
            );
        } catch (HttpClientErrorException e) {
            InputStream body = new ByteArrayInputStream(e.getResponseBodyAsByteArray());
            processResponse(res, e.getResponseHeaders(), body, e.getStatusCode());
        }
    }

    private void prepareRequest(WebScriptRequest req, ClientHttpRequest request) {

        for (String headerName : HEADERS_TO) {
            String header = req.getHeader(headerName);
            if (StringUtils.isNotBlank(header)) {
                request.getHeaders().add(headerName, header);
            }
        }

        boolean hasBody = false;

        try (InputStream in = req.getContent().getInputStream()) {

            if (in.available() > 0) {
                IOUtils.copy(in, request.getBody());
                hasBody = true;
            }
        } catch (IOException e) {
            logger.error("Error while request body writing", e);
        }

        if (hasBody) {

            String contentType = req.getParameter("_proxy_content_type");

            if (StringUtils.isBlank(contentType)) {
                contentType = req.getHeader(HttpHeaders.CONTENT_TYPE);
            }

            if (StringUtils.isNotBlank(contentType)) {
                request.getHeaders().add(HttpHeaders.CONTENT_TYPE, contentType);
            }
        }
    }

    private WebScriptResponse processResponse(WebScriptResponse res, ClientHttpResponse response) throws IOException {
        return processResponse(res, response.getHeaders(), response.getBody(), response.getStatusCode());
    }

    private WebScriptResponse processResponse(WebScriptResponse res,
                                              org.springframework.http.HttpHeaders headers,
                                              InputStream body,
                                              HttpStatus status) {

        for (String header : HEADERS_FROM) {
            List<String> values = headers.get(header);
            if (values != null && !values.isEmpty()) {
                BiConsumer<WebScriptResponse, String> consumer = HEADERS_FROM_CONSUMER.get(header);
                if (consumer != null) {
                    consumer.accept(res, values.get(0));
                } else {
                    res.setHeader(header, values.get(0));
                }
            }
        }

        try (OutputStream out = res.getOutputStream()) {
            IOUtils.copy(body, out);
            out.flush();
        } catch (IOException e) {
            logger.error("Error while response body reading", e);
        }

        res.setStatus(status.value());
        return res;
    }

    private String getFlowableUrl(String baseUrl) {

        String url = baseUrl;

        int flbUrlIndex = url.indexOf(WEBSCRIPT_URL) + WEBSCRIPT_URL.length() - 1;
        url = flowableHost + url.substring(flbUrlIndex);
        url = url.replaceAll("alf_ticket=TICKET_[^&]+&?", "");

        char lastChar = url.charAt(url.length() - 1);
        if (lastChar == '?' || lastChar == '&') {
            url = url.substring(0, url.length() - 1);
        }

        if (!url.contains("?") && url.indexOf('.', url.lastIndexOf('/')) == -1) {
            url = url + "/";
        }

        return url;
    }
}
