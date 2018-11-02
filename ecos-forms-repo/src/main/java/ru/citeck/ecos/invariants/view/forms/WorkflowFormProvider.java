package ru.citeck.ecos.invariants.view.forms;

import org.activiti.engine.TaskService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.forms.NodeViewProvider;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewElement;
import ru.citeck.ecos.invariants.view.NodeViewMode;
import ru.citeck.ecos.invariants.view.NodeViewService;
import ru.citeck.ecos.service.namespace.EcosNsPrefixResolver;


import java.io.Serializable;
import java.util.*;

@Component
public class WorkflowFormProvider implements NodeViewProvider {

    private static final String ACTIVITI_PREFIX = ActivitiConstants.ENGINE_ID + "$";

    private static final Log logger = LogFactory.getLog(WorkflowFormProvider.class);
    public static final QName ASSOC_BPM_TARGET_ITEM = QName.createQName("http://www.alfresco.org/model/bpm/1.0", "targetItem");

    @Autowired
    private NodeViewService nodeViewService;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private TaskService taskService;
    @Autowired
    @Qualifier("workflowServiceImpl")
    private WorkflowService workflowService;
    @Autowired
    private TypeFormProvider typeFormProvider;
    @Autowired
    private EcosNsPrefixResolver prefixResolver;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private DictionaryService dictionaryService;

    @Override
    public NodeViewDefinition getNodeView(String workflowId, String formId, FormMode mode, Map<String, Object> params) {
        String formKey = getFormKey(workflowId);
        return formNodeView(formKey, formId, mode, params);
    }


    public NodeViewDefinition formNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params) {
        String modeStr = "create";
        if (mode != null) {
            modeStr = mode.toString().toLowerCase();
        }
        QName type = QName.resolveToQName(namespaceService, formKey);
        NodeView query = getViewQuery(type, formId, mode, params);

        NodeViewDefinition view = new NodeViewDefinition();
        if (nodeViewService.hasNodeView(query)) {
            Map<String, Object> viewParams = new HashMap<>();
            viewParams.put("showSubmitButtons", "false");
            viewParams.put("preloadInvariants", "true");

            NodeView nodeViewToMerdge = nodeViewService.getNodeView(query);
            List<NodeViewElement> elementList = nodeViewToMerdge.getElements();

            NodeView.Builder viewBuilder = new NodeView.Builder(prefixResolver);
            viewBuilder.templateParams(viewParams);
            viewBuilder.mode(modeStr);
            viewBuilder.elements(elementList);
            viewBuilder.merge(nodeViewToMerdge);
            view.nodeView = viewBuilder.build();
        }
        view.canBeDraft = nodeViewService.canBeDraft(type);
        return view;
    }


    @Override
    public Map<String, Object> saveNodeView(String workflowId, String formId, FormMode mode,
                                            Map<String, Object> params, Map<QName, Object> attributes) {
        Map<QName, Serializable> workflowAttributes = new HashMap<>();

        for (Map.Entry<QName, Object> entry : attributes.entrySet()) {
            if (entry.getValue() instanceof Serializable) {
                if (dictionaryService.getAssociation(entry.getKey()) != null) {
                    workflowAttributes.put(entry.getKey(), convertToNode(entry.getValue()));
                } else {
                    workflowAttributes.put(entry.getKey(), (Serializable) entry.getValue());
                }
            }
        }
        NodeRef wfPackage = workflowService.createPackage(null);

        if (workflowAttributes.get(ASSOC_BPM_TARGET_ITEM) != null) {
            NodeRef docRef = (NodeRef) (workflowAttributes.get(ASSOC_BPM_TARGET_ITEM));
            nodeService.addChild(wfPackage, docRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                            QName.createValidLocalName((String) nodeService.getProperty(docRef, ContentModel.PROP_NAME))));
        }

        workflowAttributes.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        //TODO: rework with formService
        WorkflowDefinition wfDef = workflowService.getDefinitionByName(workflowId);
        workflowService.startWorkflow(wfDef.getId(), workflowAttributes);

        return Collections.emptyMap();
    }

    @Override
    public boolean hasNodeView(String workflowId, String formId, FormMode mode, Map<String, Object> params) {
        String formKey = getFormKey(workflowId);
        return typeFormProvider.hasNodeView(formKey, formId, mode, params);
    }

    private NodeView getViewQuery(QName type, String formId, FormMode mode, Map<String, Object> params) {

        NodeView.Builder builder = new NodeView.Builder(namespaceService);

        builder.className(type);
        builder.templateParams(params);

        if (formId != null) {
            builder.id(formId);
        }
        if (mode != null) {
            builder.mode(NodeViewMode.valueOf(mode.toString()));
        }
        return builder.build();
    }

    private String getFormKey(String workflowId) {

        String formKey = null;

        if (workflowId.startsWith(ACTIVITI_PREFIX)) {
            WorkflowDefinition wfDef = workflowService.getDefinitionByName(workflowId);
            formKey = wfDef.getStartTaskDefinition().getId();
        }
        return formKey;
    }

    private Serializable convertToNode(Object value) {
        if (value instanceof String) {
            return new NodeRef((String) value);
        } else if (value instanceof ArrayList) {
            ArrayList<NodeRef> refList = new ArrayList<>();
            for (Object nodeString : (ArrayList) value) {
                if (nodeString instanceof String) {
                    refList.add(new NodeRef((String) nodeString));
                }
            }
            return refList;
        } else {
            return (Serializable) value;
        }
    }

    @Override
    public void reload() {
    }

    @Override
    public String getType() {
        return "workflowId";
    }
}
