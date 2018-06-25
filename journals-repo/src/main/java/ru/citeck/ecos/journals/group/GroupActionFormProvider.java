package ru.citeck.ecos.journals.group;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.forms.NodeViewProvider;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewMode;
import ru.citeck.ecos.invariants.view.NodeViewService;
import ru.citeck.ecos.journals.JournalGroupAction;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.service.CiteckServices;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class GroupActionFormProvider implements NodeViewProvider {

    private static final Log logger = LogFactory.getLog(GroupActionFormProvider.class);

    private static final String TYPE = "groupAction";
    private static final String MODEL_NODE = "nodeRef";
    private static final String DELIMITER = "_";

    @Autowired private NodeViewService  nodeViewService;
    @Autowired private ServiceRegistry  serviceRegistry;
    private JournalService   journalService;
    private NamespaceService namespaceService;


    @PostConstruct
    private void postConstruct() {
        journalService   = (JournalService) serviceRegistry.getService(CiteckServices.JOURNAL_SERVICE);
        namespaceService = serviceRegistry.getNamespaceService();
    }

    @Override
    public NodeViewDefinition getNodeView(String viewKey, String formId, FormMode mode, Map<String, Object> params) {
        NodeViewDefinition result = new NodeViewDefinition();

        String viewClass = getViewClass(viewKey);

        if (StringUtils.isNotBlank(viewClass)) {
            QName type = QName.resolveToQName(namespaceService, viewClass);

            NodeView query = getViewQuery(type, formId, mode, params);

            if (nodeViewService.hasNodeView(query)) {
                result.nodeView = nodeViewService.getNodeView(query);
            }

            result.canBeDraft = nodeViewService.canBeDraft(type);
        }

        return result;
    }

    @Override
    public Map<String, Object> saveNodeView(String viewKey, String formId, FormMode mode, Map<String, Object> params,
                                            Map<QName, Object> attributes) {

        QName typeName  = QName.resolveToQName(namespaceService, getViewClass(viewKey));
        NodeRef nodeRef = nodeViewService.saveNodeView(typeName, formId, attributes, params);

        Map<String, Object> model = new HashMap<>();

        model.put(MODEL_NODE, nodeRef.toString());

        return model;
    }

    @Override
    public boolean hasNodeView(String viewKey, String formId, FormMode mode, Map<String, Object> params) {
        String viewClass = getViewClass(viewKey);

        if (StringUtils.isBlank(viewClass)) {
            return false;
        }

        NodeView query = getViewQuery(QName.resolveToQName(namespaceService, viewClass), formId, mode, params);

        return nodeViewService.hasNodeView(query);
    }

    @Override
    public void reload() {

    }

    @Override
    public String getType() {
        return TYPE;
    }

    private String getViewClass(String viewKey) {
        if (StringUtils.isBlank(viewKey)) {
            throw new IllegalArgumentException("viewKey is a mandatory parameter");
        }

        String[] viewKeySplit = viewKey.split(DELIMITER);

        if (viewKeySplit.length != 2) {
            throw new IllegalStateException("Argument viewKey has an invalid format: " + viewKey);
        }

        String journalId = viewKeySplit[0];
        String actionId  = viewKeySplit[1];

        JournalType journalType = journalService.getJournalType(journalId);

        if (StringUtils.isNotBlank(actionId) && journalType != null
                && CollectionUtils.isNotEmpty(journalType.getGroupActions())) {

            for (JournalGroupAction groupAction: journalType.getGroupActions()) {
                if (actionId.equals(groupAction.getId())) {
                    return groupAction.getViewClass();
                }
            }
        }

        return null;
    }

    private NodeView getViewQuery(QName type, String formId, FormMode mode, Map<String, Object> params) {
        NodeView.Builder builder = new NodeView.Builder(namespaceService)
                .className(type)
                .templateParams(params);

        if (formId != null) {
            builder.id(formId);
        }

        if (mode != null) {
            builder.mode(NodeViewMode.valueOf(mode.toString()));
        }

        return builder.build();
    }
}