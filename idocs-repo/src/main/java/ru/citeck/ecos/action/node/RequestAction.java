package ru.citeck.ecos.action.node;

/**
 * @author Pavel Simonov
 */
public class RequestAction extends URLAction {

    private static final String REQUEST_ACTION = "REQUEST";

    private static final String PROP_REQUEST_METHOD = "requestMethod";
    private static final String PROP_CONFIRMATION_MESSAGE = "confirmationMessage";
    private static final String PROP_SUCCESS_MESSAGE = "successMessage";
    private static final String PROP_SUCCESS_MESSAGE_SPAN_CLASS = "successMessageSpanClass";

    public enum RequestMethod {
        POST, GET, DELETE
    }

    public RequestAction() {
        setProperty(PROP_REQUEST_METHOD, RequestMethod.POST.name());
        setProperty(PROP_CONFIRMATION_MESSAGE, "");
        setProperty(PROP_SUCCESS_MESSAGE, "");

        ignorePropertyEquality(PROP_CONFIRMATION_MESSAGE);
        ignorePropertyEquality(PROP_SUCCESS_MESSAGE);
        ignorePropertyEquality(PROP_SUCCESS_MESSAGE_SPAN_CLASS);
    }

    public RequestMethod getMethod() {
        String method = getProperty(PROP_REQUEST_METHOD);
        return method != null ? RequestMethod.valueOf(method) : null;
    }

    public void setMethod(RequestMethod method) {
        setProperty(PROP_REQUEST_METHOD, method.name());
    }

    public String getConfirmationMessage() {
        return getProperty(PROP_CONFIRMATION_MESSAGE);
    }

    public void setConfirmationMessage(String message) {
        setProperty(PROP_CONFIRMATION_MESSAGE, message);
    }

    public String getSuccessMessage() {
        return getProperty(PROP_SUCCESS_MESSAGE);
    }

    public void setSuccessMessage(String message) {
        setProperty(PROP_SUCCESS_MESSAGE, message);
    }
    public String getSuccessMessageSpanClass() {
        return getProperty(PROP_SUCCESS_MESSAGE_SPAN_CLASS);
    }

    public void setSuccessMessageSpanClass(String spanClass) {
        setProperty(PROP_SUCCESS_MESSAGE_SPAN_CLASS, spanClass);
    }

    @Override
    protected String getActionType() {
        return REQUEST_ACTION;
    }
}
