package ru.citeck.ecos.action.node;

/**
 * @author Pavel Simonov
 */
public abstract class URLAction extends NodeActionDefinition {

    private static final String PROP_URL = "url";
    private static final String PROP_CONTEXT = "context";

    public enum URLContext {
        URL_SERVICECONTEXT, PROXY_URI, PAGECONTEXT
    }

    public URLAction() {
        setProperty(PROP_URL, null);
        setProperty(PROP_CONTEXT, URLContext.PROXY_URI.name());
    }

    public String getUrl() {
        return getProperty(PROP_URL);
    }

    public void setUrl(String url) {
        setProperty(PROP_URL, url);
    }

    public URLContext getContext() {
        String context = getProperty(PROP_CONTEXT);
        return context != null ? URLContext.valueOf(context) : null;
    }

    public void setContext(URLContext context) {
        setProperty(PROP_CONTEXT, context.name());
    }

}
