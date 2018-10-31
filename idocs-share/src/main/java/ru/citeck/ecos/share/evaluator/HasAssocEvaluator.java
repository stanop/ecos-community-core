package ru.citeck.ecos.share.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

import java.io.IOException;
import java.util.List;

public class HasAssocEvaluator extends BaseEvaluator {

    private static final String URL_TEMPLATE = "/citeck/assocs?nodeRef=%s&assocTypes=%s&addAssocs=false";
    private static final String NODE_REF_PARAM = "nodeRef";

    private ObjectMapper objectMapper;
    private String assoc;

    public HasAssocEvaluator() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private Connector getConnector(RequestContext context) throws ConnectorServiceException {
        return context != null
                ? context.getServiceRegistry()
                .getConnectorService()
                .getConnector("alfresco", context.getUserId(), ServletUtil.getSession())
                : null;
    }

    private Response callWebscript(Connector connector, String url) {
        return connector != null && url != null && !url.isEmpty()
                ? connector.call(url)
                : null;
    }

    private Body extractBody(Response response) throws IOException {
        if (response == null) {
            return null;
        }

        return response.getStatus().getCode() == Status.STATUS_OK
                ? objectMapper.readValue(response.getResponse(), Body.class)
                : null;
    }

    private boolean checkHasAssoc(JSONObject node) throws ConnectorServiceException, IOException {
        RequestContext context = ThreadLocalRequestContext.getRequestContext();
        String url = String.format(URL_TEMPLATE, node.get(NODE_REF_PARAM), assoc);
        Response response = callWebscript(getConnector(context), url);
        Body body = extractBody(response);
        return body != null && isAssocPresent(body);
    }

    private boolean isAssocPresent(Body body) {
        if (body.targets != null && !body.targets.isEmpty()) {
            return true;
        }

        if (body.children != null && !body.children.isEmpty()) {
            return true;
        }

        if (body.sources != null && !body.sources.isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean evaluate(JSONObject node) {
        try {
            return checkHasAssoc(node);
        } catch (Exception exc) {
            throw new AlfrescoRuntimeException("Problems in the work of the evaluator", exc);
        }
    }


    public void setAssoc(String assoc) {
        this.assoc = assoc;
    }


    private static class Body {
        @JsonProperty(value = "sources")
        List<Object> sources;
        @JsonProperty(value = "targets")
        List<Object> targets;
        @JsonProperty(value = "children")
        List<Object> children;
    }
}
