package ru.citeck.ecos.share.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

public class HasAssocEvaluator extends BaseEvaluator {

    private static final String URL_TEMPLATE = "/citeck/assocs?nodeRef=%s&assocTypes=%s&addAssocs=false";

    private static final String NODE_REF_PARAM = "nodeRef";

    private static final String TARGET_ASSOC_JSON_KEY = "targets";
    private static final String CHILDREN_ASSOC_JSON_KEY = "children";
    private static final String SOURCE_ASSOC_JSON_KEY = "sources";

    private String assoc;


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

    private org.json.JSONObject extractBody(Response response) throws JSONException {
        if (response == null) {
            return null;
        }

        return response.getStatus().getCode() == Status.STATUS_OK
                ? new org.json.JSONObject(response.getResponse())
                : null;
    }

    private boolean checkHasAssoc(JSONObject node) throws ConnectorServiceException, JSONException {
        RequestContext context = ThreadLocalRequestContext.getRequestContext();
        String url = String.format(URL_TEMPLATE, node.get(NODE_REF_PARAM), assoc);
        Response response = callWebscript(getConnector(context), url);
        org.json.JSONObject body = extractBody(response);
        return body != null && isAssocPresent(body);
    }

    private boolean isAssocPresent(org.json.JSONObject body) throws JSONException {
        JSONArray targetAssocs = body.getJSONArray(TARGET_ASSOC_JSON_KEY);
        if (targetAssocs != null && targetAssocs.length() > 0) {
            return true;
        }

        JSONArray childAssocs = body.getJSONArray(CHILDREN_ASSOC_JSON_KEY);
        if (childAssocs != null && childAssocs.length() > 0) {
            return true;
        }

        JSONArray sourceAssocs = body.getJSONArray(SOURCE_ASSOC_JSON_KEY);
        if (sourceAssocs != null && sourceAssocs.length() > 0) {
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
}
