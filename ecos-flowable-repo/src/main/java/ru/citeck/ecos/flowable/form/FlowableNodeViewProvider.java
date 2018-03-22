package ru.citeck.ecos.flowable.form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.form.model.FormField;
import org.flowable.form.model.FormModel;
import org.flowable.form.model.FormOutcome;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.flowable.FlowableWorkflowComponent;
import ru.citeck.ecos.flowable.form.view.FieldConverter;
import ru.citeck.ecos.flowable.services.impl.FlowableTaskServiceImpl;
import ru.citeck.ecos.flowable.services.rest.RestFormService;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.forms.NodeViewProvider;
import ru.citeck.ecos.invariants.view.NodeField;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewRegion;
import ru.citeck.ecos.service.namespace.EcosNsPrefixProvider;
import ru.citeck.ecos.service.namespace.EcosNsPrefixResolver;
import ru.citeck.ecos.utils.ConvertUtils;

import java.io.Serializable;
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
    private FlowableTaskServiceImpl taskService;
    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;

    private Map<String, FieldConverter<FormField>> fieldConverters = new HashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public NodeViewDefinition getNodeView(String taskId, String formId, FormMode mode, Map<String, Object> params) {

        NodeViewDefinition definition = new NodeViewDefinition();
        definition.canBeDraft = false;
        definition.nodeView = getFormKey(taskId).flatMap(restFormService::getFormByKey)
                                                .map(model -> {

            String modeStr = "create";
            if (mode != null) {
                modeStr = mode.toString().toLowerCase();
            }

            NodeView.Builder viewBuilder = new NodeView.Builder(prefixResolver);
            viewBuilder.template("table");
            viewBuilder.elements(getFields(model));
            viewBuilder.mode(modeStr);
            viewBuilder.templateParams(Collections.singletonMap("preloadInvariants", "true"));

            return viewBuilder.build();

        }).orElse(null);

        return definition;
    }

    private Optional<String> getFormKey(String taskId) {

        String formKey = null;

        if (taskId.startsWith(FlowableWorkflowComponent.ENGINE_PREFIX)) {

            String id = taskId.substring(taskId.indexOf("$") + 1);
            Task task = taskService.getTaskById(id);

            if (task != null) {
                formKey = task.getFormKey();
            } else {
                logger.warn("Task with id " + taskId + " not found!");
            }
        }
        return Optional.ofNullable(formKey);
    }

    @Override
    public Map<String, Object> saveNodeView(String taskId, String formId, FormMode mode,
                                            Map<String, Object> params, Map<QName, Object> attributes) {

        FormModel formModel = getFormKey(taskId).flatMap(restFormService::getFormByKey)
                                                .orElseThrow(() ->
                                                        new IllegalArgumentException(taskId + " form not found"));

        List<NodeField> fields = getFields(formModel);

        Map<QName, Serializable> taskAttributes = new HashMap<>();
        for (NodeField field : fields) {
            Object value = attributes.get(field.getAttributeName());
            Serializable taskValue = null;
            try {
                taskValue = (Serializable) ConvertUtils.convertSingleValue(value, Class.forName(field.getJavaclass()));
            } catch (ClassNotFoundException | ClassCastException e) {
                e.printStackTrace();
            }
            taskAttributes.put(QName.createQName("", field.getAttributeName().getLocalName()), taskValue);
        }

        String outcomeValue = (String) attributes.get(getOutcomeFieldName(formModel));

        workflowService.updateTask(taskId, taskAttributes, null, null);
        workflowService.endTask(taskId, outcomeValue);

        return Collections.emptyMap();
    }

    private List<NodeField> getFields(FormModel model) {

        List<NodeField> fields = new ArrayList<>();

        for (FormField field : model.getFields()) {

            FieldConverter<FormField> converter = fieldConverters.get(field.getType());
            if (converter != null) {
                fields.add(converter.convertField(field));
            } else {
                logger.error("Unregistered datatype " + field.getType());
            }
        }

        fields.add(getOutcomeField(model));

        return fields;
    }

    private NodeField getOutcomeField(FormModel formModel) {

        List<Outcome> outcomes = new ArrayList<>();
        for (FormOutcome formOutcome : formModel.getOutcomes()) {
            String id = formOutcome.getId() != null ? formOutcome.getId() : formOutcome.getName();
            outcomes.add(new Outcome(id, formOutcome.getName(), "submit"));
        }

        String buttonsConfig = "[]";
        try {
            buttonsConfig = objectMapper.writeValueAsString(outcomes);
        } catch (JsonProcessingException e) {
            logger.error(e);
        }

        NodeViewRegion.Builder regionBuilder = new NodeViewRegion.Builder(prefixResolver);
        regionBuilder.template("task-buttons");
        regionBuilder.templateParams(Collections.singletonMap("buttons", buttonsConfig));
        regionBuilder.name("input");

        NodeField.Builder fieldBuilder = new NodeField.Builder(prefixResolver);
        fieldBuilder.javaclass(String.class.getName());
        fieldBuilder.datatype(DataTypeDefinition.TEXT);
        fieldBuilder.property(getOutcomeFieldName(formModel));
        fieldBuilder.regions(Collections.singletonList(regionBuilder.build()));
        fieldBuilder.template("row");

        return fieldBuilder.build();
    }

    private QName getOutcomeFieldName(FormModel formModel) {
        String outcomeFieldName = formModel.getOutcomeVariableName();
        if (outcomeFieldName == null) {
            outcomeFieldName = "outcome";
        }
        return QName.createQName(FLOWABLE_DEFAULT_NS_URI, outcomeFieldName);
    }

    @Override
    public boolean hasNodeView(String taskId, String formId, FormMode mode, Map<String, Object> params) {
        return getFormKey(taskId).map(restFormService::hasFormWithKey).orElse(false);
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

    private static class Outcome {

        public String title;
        public String actionId;
        public String value;

        public Outcome(String value, String title, String actionId) {
            this.value = value;
            this.title = title;
            this.actionId = actionId;
        }
    }

}
