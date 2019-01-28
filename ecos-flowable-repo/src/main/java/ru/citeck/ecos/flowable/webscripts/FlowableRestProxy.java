package ru.citeck.ecos.flowable.webscripts;

import org.alfresco.repo.admin.SysAdminParams;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.webscripts.*;
import org.springframework.http.HttpMethod;
import ru.citeck.ecos.flowable.services.rest.FlowableRestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FlowableRestProxy extends AbstractWebScript {

    private static final String WEBSCRIPT_URL = "citeck/flowable/proxy/";
    private static final String FLOWABLE_HOST_KEY = "${flowable.host.url}";

    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    private static final String[] HEADERS_TO_SEND = {
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

        restTemplate.execute(url, method, request -> {

            for (String headerName : HEADERS_TO_SEND) {
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
                //do nothing
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
        }, response -> {

            response.getHeaders().forEach((key, values) -> {
                if (values.size() == 0) {
                    return;
                }
                String value = values.get(0);
                switch (key) {
                    case HttpHeaders.CONTENT_TYPE:
                        res.setContentType(value);
                        break;
                    case HttpHeaders.CONTENT_LENGTH:
                        res.setHeader(key, value);
                        break;
                    case HttpHeaders.CONTENT_ENCODING:
                        res.setContentEncoding(value);
                        break;
                    case HEADER_CONTENT_DISPOSITION:
                        res.setHeader(HEADER_CONTENT_DISPOSITION, value);
                        break;
                }
            });

            try (OutputStream out = res.getOutputStream()) {
                IOUtils.copy(response.getBody(), out);
            }

            res.setStatus(response.getStatusCode().value());

            return null;
        });
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

        if (!url.contains("?") && url.indexOf(".", url.lastIndexOf("/")) == -1) {
            url = url + "/";
        }

        return url;
    }
}
