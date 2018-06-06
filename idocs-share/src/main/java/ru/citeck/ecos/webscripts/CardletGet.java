package ru.citeck.ecos.webscripts;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.ServerConfigElement;
import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.surf.ModelObject;
import org.springframework.extensions.surf.ModelObjectService;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.ExtensibilityModel;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.types.AbstractModelObject;
import org.springframework.extensions.surf.types.Component;
import org.springframework.extensions.surf.uri.UriUtils;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardletGet extends AbstractWebScript {

    private static final String PARAM_REGION_ID = "regionId";

    private ModelObjectService modelObjectService;
    private LocalWebScriptRuntimeContainer webScriptsContainer;
    private ConfigService configService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String regionId = req.getParameter(PARAM_REGION_ID);

        Component component = modelObjectService.getComponent("page", regionId, "card-details");

        Map<String, String> params = new HashMap<>();
        for (String paramName : req.getParameterNames()) {
            params.put(paramName, req.getParameter(paramName));
        }

        Map<String, Serializable> compProps = component.getProperties();

        compProps.forEach((k, v) -> {

            String valueStr = String.valueOf(v);

            Pattern p = Pattern.compile("\\{(\\S+)}");
            Matcher m = p.matcher(valueStr);

            StringBuilder valueBuilder = new StringBuilder(valueStr);

            int idx = 0;
            while (m.find(idx)) {
                String key = m.group(1);
                String reqValue = req.getParameter(key);
                if (reqValue != null) {
                    valueBuilder.replace(m.regionStart(), m.regionEnd(), reqValue);
                }
                idx = m.regionEnd();
            }

            params.put(k, valueBuilder.toString());
        });

        res.setContentType(Format.HTML.mimetype() + ";charset=UTF-8");

        runWebScript(component.getURI(), res.getWriter(), params);

        res.setStatus(Status.STATUS_OK);
    }

    /**
     * Run a WebScript against the supplied {@link Writer}. This by-passes the {@link ExtensibilityModel} and so that
     * we can build a cache file directly from the processed content.
     *
     * @param uri    String
     * @param writer Writer
     */
    private void runWebScript(String uri, Writer writer, Map<String, String> params) {

        RequestContext context = ThreadLocalRequestContext.getRequestContext();

        // Construct a "context" object that the Web Script engine will utilise
        LocalWebScriptContext webScriptContext = new LocalWebScriptContext();

        // Copy in request parameters into a HashMap
        // This is so as to be compatible with UriUtils (and Token substitution)
        webScriptContext.setTokens(params);
        //context.getParameters()

        // Begin to process the actual web script
        // Get the web script url, perform token substitution and remove query string
        String url = UriUtils.replaceTokens(uri, context, null, null, "");
        webScriptContext.setScriptUrl((url.indexOf('?') == -1 ? url : url.substring(0, url.indexOf('?'))));

        // Get up the request path.
        // If none is supplied, assume the servlet path.
        String requestPath = (String) context.getValue("requestPath");
        if (requestPath == null) {
            requestPath = context.getContextPath();
        }

        webScriptContext.setExecuteUrl(requestPath + WebScriptProcessor.WEBSCRIPT_SERVICE_SERVLET + url);

        // Set up state onto the local web script context
        webScriptContext.setRuntimeContainer(this.webScriptsContainer);
        webScriptContext.setRequestContext(context);

        ModelObject dmo = new DummyModelObject(params);
        webScriptContext.setModelObject(dmo);

        ServerProperties serverProperties;
        Config config = this.configService.getConfig("Server");
        serverProperties = (ServerConfigElement) config.getConfigElement(ServerConfigElement.CONFIG_ELEMENT_ID);

        // Construct the Web Script Runtime
        // This bundles the container, the context and the encoding
        LocalWebScriptRuntime runtime = new LocalWebScriptRuntime(writer, this.webScriptsContainer, serverProperties, webScriptContext);

        // set the method onto the runtime
        if (context.getRequestMethod() != null) {
            runtime.setScriptMethod(LocalWebScriptRuntime.DEFAULT_METHOD_GET);
        }

        // Suppress extensibility on the container...
        // This has the effect of ensuring that the WebScript doesn't attempt to output the content into an extensibility
        // model but will instead write the content directly to the StringWriter provided. This means that we can then grab
        // the generated response directly...
        webScriptsContainer.suppressExtensibility();
        webScriptsContainer.bindRequestContext(context);
        try {
            runtime.executeScript();
        } finally {
            // Make sure to unsupress extensibility on the container!!
            webScriptsContainer.unsuppressExtensibility();
            webScriptsContainer.unbindRequestContext();
        }
    }

    public void setModelObjectService(ModelObjectService modelObjectService) {
        this.modelObjectService = modelObjectService;
    }

    public void setWebScriptsContainer(LocalWebScriptRuntimeContainer webScriptsContainer) {
        this.webScriptsContainer = webScriptsContainer;
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * This inner-class is required to run the WebScript for generating the default constants file (see the runWebScript method)
     * <br/><br/>author dave
     */
    @SuppressWarnings("serial")
    private class DummyModelObject extends AbstractModelObject {
        private Map<String, String> params;

        public DummyModelObject(Map<String, String> params) {
            this.params = params;
        }

        @Override
        public String getTypeId() {
            return null;
        }

        @Override
        public Map<String, Serializable> getProperties() {
            return new HashMap<>(params);
        }

        @Override
        public Map<String, Serializable> getCustomProperties() {
            return new HashMap<>();
        }
    }
}
