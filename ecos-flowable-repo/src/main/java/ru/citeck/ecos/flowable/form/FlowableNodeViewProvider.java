package ru.citeck.ecos.flowable.form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.TaskService;
import org.flowable.form.model.FormField;
import org.flowable.form.model.FormOutcome;
import org.flowable.form.model.SimpleFormModel;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.attr.NodeAttributeService;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.form.view.AssocFieldConverter;
import ru.citeck.ecos.flowable.form.view.FieldConverter;
import ru.citeck.ecos.flowable.services.FlowableCustomCommentService;
import ru.citeck.ecos.flowable.services.impl.FlowableTaskServiceImpl;
import ru.citeck.ecos.flowable.services.rest.RestFormService;
import ru.citeck.ecos.form.WorkflowFormProvider;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.forms.NodeViewProvider;
import ru.citeck.ecos.invariants.view.NodeField;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewRegion;
import ru.citeck.ecos.invariants.view.forms.TypeFormProvider;
import ru.citeck.ecos.service.namespace.EcosNsPrefixProvider;
import ru.citeck.ecos.service.namespace.EcosNsPrefixResolver;
import ru.citeck.ecos.utils.ConvertUtils;
import ru.citeck.ecos.utils.WorkflowUtils;
import ru.citeck.ecos.workflow.tasks.EcosTaskService;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FlowableNodeViewProvider implements NodeViewProvider, EcosNsPrefixProvider {

    public static final String NS_URI_DEFAULT = "http://www.flowable.org";
    public static final String NS_PREFIX_DEFAULT = "flb";
    public static final String DOCUMENT_FIELD_PREFIX = "_ECM_";

    private static final String OUTCOME_LABEL_KEY_DEFAULT = "flowable.task.button.default-complete.label";
    private static final String OUTCOME_LABEL_KEY_TEMPLATE = "flowable.form.button.%s.%s.label";
    private static final String OUTCOME_ID_KEY_DEFAULT = "flowable.task.button.default-complete.id";
    private static final String OUTCOME_ACTION_SUBMIT = "submit";

    private static final String ACTIVITI_PREFIX = ActivitiConstants.ENGINE_ID + "$";

    private static final Log logger = LogFactory.getLog(FlowableNodeViewProvider.class);

    @Autowired
    private EcosNsPrefixResolver prefixResolver;
    @Autowired
    private RestFormService restFormService;
    @Autowired
    private FlowableTaskServiceImpl flowableTaskService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FlowableCustomCommentService flowableCustomCommentService;
    @Autowired
    private TypeFormProvider typeFormProvider;
    @Autowired
    private org.activiti.engine.TaskService activitiTaskService;
    @Autowired
    private WorkflowFormProvider workflowFormProvider;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private NodeAttributeService nodeAttributeService;
    @Autowired
    private WorkflowUtils workflowUtils;
    @Autowired
    private EcosTaskService ecosTaskService;

    private Map<String, FieldConverter<FormField>> fieldConverters = new HashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public NodeViewDefinition getNodeView(String taskId, String formId, FormMode mode, Map<String, Object> params) {

        NodeViewDefinition definition = null;

        if (taskId.startsWith(FlowableConstants.ENGINE_PREFIX)) {

            definition = new NodeViewDefinition();

            definition.canBeDraft = false;

            Optional<SimpleFormModel> formModel = getFormKey(taskId).flatMap(restFormService::getFormByKey);
            if (formModel.isPresent()) {

                String id = taskId.substring(taskId.indexOf("$") + 1);
                Map<String, Object> variables = taskService.getVariables(id);
                Map<String, Object> localVariables = taskService.getVariablesLocal(id);

                List<String> commentFields = new ArrayList<>();
                variables.forEach((varId, value) -> {
                    if (varId.endsWith("_comment") && !localVariables.containsKey(varId)) {
                        commentFields.add(varId);
                    }
                });
                commentFields.forEach(variables::remove);

                List<String> commentFieldIds = flowableCustomCommentService.getFieldIdsByTaskId(id);
                commentFieldIds.forEach(commentFieldId -> {
                    variables.remove(commentFieldId);
                    taskService.removeVariable(id, commentFieldId);
                });
                definition.nodeView = getNodeView(formModel.get(), mode, variables);
            }

        } else if (taskId.startsWith(ACTIVITI_PREFIX)) {

            Optional<String> formKey = getFormKey(taskId);
            if (formKey.isPresent()) {
                definition = workflowFormProvider.formNodeView(formKey.get(), formId, mode, params);
            }
        }

        return definition != null ? definition : new NodeViewDefinition();
    }

    private NodeView getNodeView(SimpleFormModel model, FormMode mode, Map<String, Object> variables) {

        String modeStr = "create";
        if (mode != null) {
            modeStr = mode.toString().toLowerCase();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("preloadInvariants", "true");
        params.put("showSubmitButtons", "false");

        NodeView.Builder viewBuilder = new NodeView.Builder(prefixResolver);
        viewBuilder.template("table");
        viewBuilder.elements(getFields(model, mode, variables));
        viewBuilder.mode(modeStr);
        viewBuilder.templateParams(params);

        return viewBuilder.build();
    }

    private Optional<String> getFormKey(String taskId) {

        String formKey = null;

        if (taskId.startsWith(FlowableConstants.ENGINE_PREFIX)) {

            String id = taskId.substring(taskId.indexOf("$") + 1);
            Task task = flowableTaskService.getTaskById(id);

            if (task != null) {
                formKey = task.getFormKey();
            } else {
                logger.warn("Task with id " + taskId + " not found!");
            }
        } else if (taskId.startsWith(ACTIVITI_PREFIX)) {
            String id = taskId.substring(taskId.indexOf("$") + 1);
            org.activiti.engine.task.Task task = activitiTaskService.createTaskQuery().taskId(id).singleResult();
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

        Map<String, Object> taskAttributes;


        if (taskId.startsWith(FlowableConstants.ENGINE_PREFIX)) {
            SimpleFormModel formModel = getFormKey(taskId).flatMap(restFormService::getFormByKey)
                    .orElseThrow(() ->
                            new IllegalArgumentException(taskId + " form not found"));

            List<NodeField> fields = getFields(formModel, mode, Collections.emptyMap());

            taskAttributes = new HashMap<>();
            for (NodeField field : fields) {
                Object value = attributes.get(field.getAttributeName());
                Serializable taskValue = null;
                try {
                    taskValue = (Serializable) ConvertUtils.convertSingleValue(value, Class.forName(field.getJavaclass()));
                } catch (ClassNotFoundException | ClassCastException e) {
                    e.printStackTrace();
                }
                taskAttributes.put(field.getAttributeName().getLocalName(), taskValue);
            }

            Map<QName, Object> documentAttributes = new HashMap<>();
            taskAttributes.forEach((name, value) -> {
                if (name.startsWith(DOCUMENT_FIELD_PREFIX)) {
                    String docFieldName = name.replace(DOCUMENT_FIELD_PREFIX, "")
                                              .replace('_', ':');

                    QName docFieldQName = QName.resolveToQName(namespaceService, docFieldName);

                    documentAttributes.put(docFieldQName, value);
                }
            });

            if (!documentAttributes.isEmpty()) {

                String taskLocalId = taskId.substring(taskId.indexOf("$") + 1);
                Object bpmPackage = taskService.getVariable(taskLocalId, "bpm_package");

                NodeRef documentRef = getTaskDocument(bpmPackage);

                if (documentRef != null) {
                    nodeAttributeService.setAttributes(documentRef, documentAttributes);
                }
            }
        } else {
            taskAttributes = new HashMap<>();
            for (Map.Entry<QName, Object> entry : attributes.entrySet()) {
                taskAttributes.put(entry.getKey().toString(), entry.getValue());
            }
        }

        ecosTaskService.endTask(taskId, taskAttributes);

        return Collections.emptyMap();
    }

    private List<NodeField> getFields(SimpleFormModel model, FormMode mode, Map<String, Object> values) {

        List<NodeField> fields = new ArrayList<>();
        Map<String, Object> fieldsValues = new HashMap<>(values);

        boolean documentNotReady = true;
        NodeRef documentRef = null;
        Map<QName, Serializable> properties = null;

        for (FormField field : model.getFields()) {

            String id = field.getId();
            String fieldType = field.getType();

            if (id.startsWith(DOCUMENT_FIELD_PREFIX)) {

                if (documentNotReady) {
                    documentRef = getTaskDocument(values);
                    documentNotReady = false;
                }

                String attName = id.substring(DOCUMENT_FIELD_PREFIX.length()).replace("_", ":");
                QName attQName = QName.resolveToQName(namespaceService, attName);

                Object defaultValue;

                if (dictionaryService.getProperty(attQName) != null) {

                    if (properties == null) {
                        if (documentRef != null) {
                            properties = nodeService.getProperties(documentRef);
                        } else {
                            properties = Collections.emptyMap();
                        }
                    }

                    defaultValue = properties.get(attQName);

                } else {

                    fieldType = AssocFieldConverter.TYPE;

                    if (documentRef != null) {
                        List<AssociationRef> assocs = nodeService.getTargetAssocs(documentRef, attQName);
                        defaultValue = assocs.stream()
                                             .map(AssociationRef::getTargetRef)
                                             .collect(Collectors.toList());
                    } else {
                        defaultValue = Collections.emptyList();
                    }
                }

                if (defaultValue != null) {
                    fieldsValues.put(id, defaultValue);
                }
            }

            FieldConverter<FormField> converter = fieldConverters.get(fieldType);
            if (converter != null) {
                fields.add(converter.convertField(field, fieldsValues));
            } else {
                logger.error("Unregistered datatype " + field.getType());
            }
        }

        if (mode == null || mode.equals(FormMode.EDIT)
                || mode.equals(FormMode.CREATE)) {
            fields.add(getOutcomeField(model));
        }

        return fields;
    }

    private NodeRef getTaskDocument(Map<String, Object> props) {
        return getTaskDocument(props.get("bpm_package"));
    }

    private NodeRef getTaskDocument(Object bpmPackage) {
        return workflowUtils.getTaskDocumentFromPackage(bpmPackage);
    }

    private NodeField getOutcomeField(SimpleFormModel formModel) {

        List<Outcome> outcomes = new ArrayList<>();
        String formKey = StringUtils.isNotBlank(formModel.getKey()) ? formModel.getKey() : formModel.getName();
        for (FormOutcome formOutcome : formModel.getOutcomes()) {
            String id = formOutcome.getId() != null ? formOutcome.getId() : formOutcome.getName();
            String outcomeLabel = I18NUtil.getMessage(String.format(OUTCOME_LABEL_KEY_TEMPLATE, formKey, id));
            if (StringUtils.isBlank(outcomeLabel)) {
                outcomeLabel = formOutcome.getName();
            }
            outcomes.add(new Outcome(id, outcomeLabel, OUTCOME_ACTION_SUBMIT));
        }

        if (outcomes.isEmpty()) {
            String label = getMessage(OUTCOME_LABEL_KEY_DEFAULT);
            String id = getMessage(OUTCOME_ID_KEY_DEFAULT);
            outcomes.add(new Outcome(id, label, OUTCOME_ACTION_SUBMIT));
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

    private String getMessage(String key) {
        String result = I18NUtil.getMessage(OUTCOME_LABEL_KEY_DEFAULT);
        return result != null ? result : key;
    }

    private QName getOutcomeFieldName(SimpleFormModel formModel) {
        String outcomeFieldName = formModel.getOutcomeVariableName();
        if (outcomeFieldName == null) {
            outcomeFieldName = "form_" + formModel.getKey() + "_outcome";
        }
        return QName.createQName(NS_URI_DEFAULT, outcomeFieldName);
    }

    @Override
    public boolean hasNodeView(String taskId, String formId, FormMode mode, Map<String, Object> params) {
        if (taskId.startsWith(ACTIVITI_PREFIX)) {
            return typeFormProvider.hasNodeView(getFormKey(taskId).get(), formId, mode, params);
        } else if (taskId.startsWith(FlowableConstants.ENGINE_PREFIX)) {
            return getFormKey(taskId).map(restFormService::hasFormWithKey).orElse(false);
        }
        return false;
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
        return Collections.singletonMap(NS_URI_DEFAULT, NS_PREFIX_DEFAULT);
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
