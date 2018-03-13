package ru.citeck.ecos.share.evaluator;


import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

/**
 * @author Roman Velikoselsky
 */

public class IsCaseRolesEvaluator extends BaseEvaluator {

    private static final String URL_TEMPLATE = "/citeck/is-role-member?nodeRef=%s&user=%s&role=%s";

    private String role;

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

    private boolean checkCaseRole(JSONObject node) throws ConnectorServiceException, JSONException {
        RequestContext context = ThreadLocalRequestContext.getRequestContext();
        String userId = context.getUserId();
        String url = String.format(URL_TEMPLATE, node.get("nodeRef"), userId, role);
        Response response = callWebscript(getConnector(context), url);
        org.json.JSONObject body = extractBody(response);
        return body != null && body.getBoolean("isRoleMember");
    }

    @Override
    public boolean evaluate(JSONObject node) {
        try {
            return checkCaseRole(node);
        } catch (Exception exc) {
            throw new AlfrescoRuntimeException("Problems in the work of the evaluator", exc);
        }
    }

    public void setRole(String role) {
        this.role = role;
    }
}