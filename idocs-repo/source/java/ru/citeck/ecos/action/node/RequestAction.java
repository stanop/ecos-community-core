package ru.citeck.ecos.action.node;

/**
 * @author Pavel Simonov
 */
public class RequestAction extends URLAction {

    private static final String REQUEST_ACTION = "REQUEST";

    private static final String PROP_REQUEST_METHOD = "requestMethod";

    public enum RequestMethod {
        POST, GET, DELETE
    }

    public RequestAction() {
        setProperty(PROP_REQUEST_METHOD, RequestMethod.POST.name());
    }

    public RequestMethod getMethod() {
        String method = getProperty(PROP_REQUEST_METHOD);
        return method != null ? RequestMethod.valueOf(method) : null;
    }

    public void setMethod(RequestMethod method) {
        setProperty(PROP_REQUEST_METHOD, method.name());
    }

    @Override
    protected String getActionType() {
        return REQUEST_ACTION;
    }
}
