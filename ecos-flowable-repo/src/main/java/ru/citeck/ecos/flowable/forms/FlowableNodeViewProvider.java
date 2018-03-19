package ru.citeck.ecos.flowable.forms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.form.model.FormField;
import org.flowable.form.model.FormModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.flowable.services.rest.RestFormService;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.forms.NodeViewProvider;
import ru.citeck.ecos.invariants.view.NodeField;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewRegion;
import ru.citeck.ecos.utils.ResourceResolver;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FlowableNodeViewProvider implements NodeViewProvider {

    private static final Log logger = LogFactory.getLog(FlowableNodeViewProvider.class);

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private ResourceResolver resourceResolver;

    @Autowired
    private RestFormService restFormService;

    private ConcurrentHashMap<String, NodeViewDefinition> views = new ConcurrentHashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {

    }

    @Override
    public NodeViewDefinition getNodeView(String formKey, String formId, FormMode mode, Map<String, String> params) {

        Optional<FormModel> form = restFormService.getFormByKey(formKey);

        Optional<NodeViewDefinition> result = form.map(model -> {

            NodeView.Builder viewBuilder = new NodeView.Builder(namespaceService);

            //viewBuilder.template("rowset");
            viewBuilder.template("table");

            List<NodeField> fields = new ArrayList<>();

            for (FormField field : model.getFields()) {

                NodeField.Builder fieldBuilder = new NodeField.Builder(namespaceService);
                fieldBuilder.property(field.getId());
                fieldBuilder.template("row");

                List<NodeViewRegion> regions = new ArrayList<>();

                NodeViewRegion.Builder regionBuilder = new NodeViewRegion.Builder(namespaceService);
                regionBuilder.name("label");
                regionBuilder.template("label");
                regionBuilder.templateParams(Collections.singletonMap("text", field.getName()));
                regions.add(regionBuilder.build());

                regionBuilder = new NodeViewRegion.Builder(namespaceService);
                regionBuilder.name("input");

                Map<String, Object> templateParams;

                switch (field.getType()) {

                    case "text":
                        fieldBuilder.datatype(DataTypeDefinition.TEXT);
                        regionBuilder.template("text");
                        break;

                    case "integer":
                        fieldBuilder.datatype(DataTypeDefinition.INT);
                        regionBuilder.template("number");

                        templateParams = new HashMap<>();
                        templateParams.put("step", "1");
                        templateParams.put("isInteger", "true");
                        regionBuilder.templateParams(templateParams);
                        break;

                    case "boolean":
                        fieldBuilder.datatype(DataTypeDefinition.BOOLEAN);
                        regionBuilder.template("checkbox");
                        break;

                    default:
                        logger.error("Unregistered datatype " + field.getType());
                        continue;
                }

                regions.add(regionBuilder.build());

                fieldBuilder.regions(regions);
                fields.add(fieldBuilder.build());
            }

            viewBuilder.elements(fields);
            viewBuilder.mode("create");

            NodeViewDefinition definition = new NodeViewDefinition();
            definition.nodeView = viewBuilder.build();
            definition.canBeDraft = false;

            return definition;

        });

        return result.orElse(null);
    }

    @Override
    public String getType() {
        return "taskId";
    }

    @Override
    public void reload() {

    }

}
