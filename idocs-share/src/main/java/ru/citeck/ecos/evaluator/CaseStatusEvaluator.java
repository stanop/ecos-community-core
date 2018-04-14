package ru.citeck.ecos.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

import java.util.List;

/**
 * Evaluator for checking the case for compliance with the
 * statuses.
 * 
 * @autor Andrew Timokhin
 */
public class CaseStatusEvaluator extends BaseEvaluator {
    
    private static final String ASPECT_HAS_CASE_STATUS = "icase:hasCaseStatus";
    private static final String URL_TEMPLATE           = "/citeck/case/is-case-in-statuses?nodeRef=%s&statuses=%s";
    private static final char   DELIMITER              = ',';
    
    private List<String> statuses;

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }
    
    private String getListAsString(List<String> list, char delimiter) {
        if (CollectionUtils.isEmpty(statuses)) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            String status = list.get(i);

            if (status != null) {
                result.append(status);

                if (i != list.size() - 1) {
                    result.append(delimiter);
                }
            }
        }
        
        return result.toString();
    }

    private boolean hasCaseStatus(JSONObject node) {
        JSONArray aspects = getNodeAspects(node);
        return aspects != null && aspects.contains(ASPECT_HAS_CASE_STATUS);
    }
    
    private Connector getConnector() throws ConnectorServiceException {
        RequestContext context = ThreadLocalRequestContext.getRequestContext();

        return context.getServiceRegistry()
                      .getConnectorService()
                      .getConnector("alfresco", context.getUserId(), ServletUtil.getSession());
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
    
    private boolean checkStatuses(JSONObject node) throws ConnectorServiceException, JSONException {
        String url = String.format(URL_TEMPLATE, node.get("nodeRef"), getListAsString(statuses, DELIMITER));
        Response response = callWebscript(getConnector(), url);
        org.json.JSONObject body = extractBody(response);
        return body != null && body.getBoolean("data");
    }
    
    @Override
    public boolean evaluate(JSONObject node) {
        if (!hasCaseStatus(node)) {
            return false;
        }

        try {
            return checkStatuses(node);
        } catch (Exception exc) {
            throw new AlfrescoRuntimeException("Problems in the work of the evaluator", exc);
        }
    }
}