package ru.citeck.ecos.flowable.form;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.form.model.FormField;
import org.flowable.form.model.FormModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.flowable.form.view.FieldConverter;
import ru.citeck.ecos.flowable.services.rest.RestFormService;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.forms.NodeViewProvider;
import ru.citeck.ecos.invariants.view.NodeField;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.service.namespace.EcosNsPrefixProvider;
import ru.citeck.ecos.service.namespace.EcosNsPrefixResolver;

import java.util.*;

@Component
public class FlowableNodeViewProvider implements NodeViewProvider, EcosNsPrefixProvider {

    public static final String FLOWABLE_DEFAULT_NS_URI = "http://www.flowable.org";
    public static final String FLOWABLE_DEFAULT_NS_PREFIX = "flb";

    private static final Log logger = LogFactory.getLog(FlowableNodeViewProvider.class);

    @Autowired
    private EcosNsPrefixResolver prefixResolver;
    @Autowired
    private RestFormService restFormService;
    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;

    private Map<String, FieldConverter<FormField>> fieldConverters = new HashMap<>();

    @Override
    public NodeViewDefinition getNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params) {

        Optional<FormModel> form = restFormService.getFormByKey(formKey);

        Optional<NodeViewDefinition> result = form.map(model -> {

            NodeView.Builder viewBuilder = new NodeView.Builder(prefixResolver);

            viewBuilder.template("table");

            List<NodeField> fields = new ArrayList<>();

            for (FormField field : model.getFields()) {

                FieldConverter<FormField> converter = fieldConverters.get(field.getType());
                if (converter != null) {
                    fields.add(converter.convertField(field));
                } else {
                    logger.error("Unregistered datatype " + field.getType());
                }
            }

            viewBuilder.elements(fields);
            viewBuilder.mode("create");

            viewBuilder.templateParams(Collections.singletonMap("preloadInvariants", "true"));

            NodeViewDefinition definition = new NodeViewDefinition();
            definition.nodeView = viewBuilder.build();
            definition.canBeDraft = false;

            return definition;
        });

        return result.orElse(null);
    }

    @Override
    public Map<String, Object> saveNodeView(String formKey, String formId, FormMode mode,
                                            Map<String, Object> params, Map<QName, Object> attributes) {



        return Collections.emptyMap();
    }

    @Override
    public boolean hasNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params) {
        return restFormService.hasFormWithKey(formKey);
    }

    @Override
    public String getType() {
        return "taskId";
    }

    @Override
    public void reload() {
    }

    @Autowired
    @SuppressWarnings("unchecked")
    public void setFieldConverters(List<FieldConverter<?>> converters) {
        for (FieldConverter converter : converters) {
            fieldConverters.put(converter.getSupportedFieldType(), converter);
        }
    }

    @Override
    public Map<String, String> getPrefixesByNsURI() {
        return Collections.singletonMap(FLOWABLE_DEFAULT_NS_URI, FLOWABLE_DEFAULT_NS_PREFIX);
    }
}
