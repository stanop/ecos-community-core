package ru.citeck.ecos.webscripts.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Format;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.forms.EcosFormService;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.model.InvariantsModel;
import ru.citeck.ecos.service.namespace.EcosNsPrefixResolver;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EcosFormNodeViewPost extends AbstractWebScript {

    /*========PARAMS========*/
    private static final String PARAM_FORM_TYPE = "formType";
    private static final String PARAM_FORM_MODE = "formMode";
    private static final String PARAM_FORM_KEY = "formKey";
    private static final String PARAM_FORM_ID = "formId";
    /*=======/PARAMS========*/

    private static final String RESULT_KEY = "result";

    @Autowired
    private EcosFormService ecosFormService;
    @Autowired
    private EcosNsPrefixResolver prefixResolver;
    @Autowired
    private TransactionService transactionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        res.setContentEncoding("utf-8");

        String formType = req.getParameter(PARAM_FORM_TYPE);
        String formModeStr = req.getParameter(PARAM_FORM_MODE);
        String formKey = req.getParameter(PARAM_FORM_KEY);
        String formId = req.getParameter(PARAM_FORM_ID);

        FormMode mode = null;
        if (formModeStr != null) {
            mode = Enum.valueOf(FormMode.class, formModeStr.toUpperCase());
        }

        PostContent postContent = objectMapper.readValue(req.getContent().getReader(), PostContent.class);

        Map<QName, Object> attributes = keysAsQName(postContent.attributes);
        if (postContent.isDraft != null) {
            attributes.put(InvariantsModel.PROP_IS_DRAFT, postContent.isDraft);
        }

        Map<String, Object> resp = saveNodeView(req, formType, formKey, formId, mode, postContent, attributes);

        Map<String, Object> result = new HashMap<>(1);
        result.put(RESULT_KEY, resp);
        objectMapper.writeValue(res.getWriter(), result);
    }

    private Map<String, Object> saveNodeView(WebScriptRequest req, String formType, String formKey, String formId,
                                             FormMode mode, PostContent postContent, Map<QName, Object> attributes) {

        RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        return helper.doInTransaction(() -> {
                return ecosFormService.saveNodeView(formType, formKey, formId, mode,
                    getParams(req, postContent), attributes);
            },
            false, true);
    }

    private Map<QName, Object> keysAsQName(Map<String, Object> attributes) {
        Map<QName, Object> result = new HashMap<>();
        attributes.forEach((k, v) -> result.put(QName.resolveToQName(prefixResolver, k), v));
        return result;
    }

    private Map<String, Object> getParams(WebScriptRequest req, PostContent postContent) {
        Map<String, Object> result = new HashMap<>();

        NodeView viewModel = postContent.view;
        Map<String, Object> viewParams = viewModel != null ? viewModel.params : null;
        if (viewParams != null) {
            result.putAll(viewParams);
        }

        for (String key : req.getParameterNames()) {
            result.put(key, req.getParameter(key));
        }
        return result;
    }

    private static class PostContent {
        public NodeView view;
        public Map<String, Object> attributes;
        public Boolean isDraft;
    }

    private static class NodeView {
        public String mode;
        public String template;
        public String kind;
        public String id;
        public Map<String, Object> params;
        @JsonProperty(value = "class")
        public String clazz;
    }
}
