package ru.citeck.ecos.action;

import java.util.*;

/**
 * @author deathNC on 30.04.2016.
 * @author Pavel Simonov
 */
public class NodeActionDefinition {

    public static final String PROP_ACTION_TYPE = "actionType";
    public static final String PROP_CONTEXT = "context";
    public static final String PROP_TITLE = "title";
    public static final String PROP_URL = "url";
    public static final String PROP_REQUEST_METHOD = "method";

    private final static List<String> EQUALITY_PROPERTIES = Arrays.asList(PROP_REQUEST_METHOD,
                                                                          PROP_ACTION_TYPE,
                                                                          PROP_CONTEXT,
                                                                          PROP_URL);

    public enum NodeActionType {
        REQUEST, REDIRECT, SHOW_POPUP
    }

    public enum URLContext {
        URL_SERVICECONTEXT, PROXY_URI
    }

    public enum RequestMethod {
        POST, GET, DELETE
    }

    private final Map<String, String> properties = new HashMap<>();


    public NodeActionDefinition() {
        setProperty(PROP_REQUEST_METHOD, RequestMethod.POST.name());
        setProperty(PROP_CONTEXT, URLContext.PROXY_URI.name());
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public String getTitle() {
        return getProperty(PROP_TITLE);
    }

    public NodeActionDefinition setTitle(String title) {
        setProperty(PROP_TITLE, title);
        return this;
    }

    public String getUrl() {
        return getProperty(PROP_URL);
    }

    public NodeActionDefinition setUrl(String url) {
        setProperty(PROP_URL, url);
        return this;
    }

    public NodeActionType getActionType() {
        return NodeActionType.valueOf(properties.get(PROP_ACTION_TYPE));
    }

    public NodeActionDefinition setActionType(NodeActionType actionType) {
        setProperty(PROP_ACTION_TYPE, actionType.name());
        return this;
    }

    public URLContext getContext() {
        return URLContext.valueOf(getProperty(PROP_CONTEXT));
    }

    public NodeActionDefinition setContext(URLContext context) {
        setProperty(PROP_CONTEXT, context.name());
        return this;
    }

    public NodeActionDefinition setMethod(RequestMethod method) {
        setProperty(PROP_REQUEST_METHOD, method.name());
        return this;
    }

    public RequestMethod getMethod() {
        return RequestMethod.valueOf(getProperty(PROP_REQUEST_METHOD));
    }

    public NodeActionDefinition setProperty(String key, Object value) {
        properties.put(key, value == null ? null : value.toString());
        return this;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeActionDefinition that = (NodeActionDefinition) o;
        for (String prop : EQUALITY_PROPERTIES) {
            if (!Objects.equals(getProperty(prop), that.getProperty(prop))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }
}
