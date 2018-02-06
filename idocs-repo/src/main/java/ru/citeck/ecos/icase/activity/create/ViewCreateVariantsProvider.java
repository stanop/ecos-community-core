package ru.citeck.ecos.icase.activity.create;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewMode;
import ru.citeck.ecos.invariants.view.NodeViewService;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.ICaseTaskModel;

import java.util.*;

public class ViewCreateVariantsProvider extends CreateVariantsProvider {

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private NodeViewService nodeViewService;
    private MessageService messageService;

    private Set<QName> ignoredTypes = new HashSet<>();

    @Override
    public List<ActivityCreateVariant> getCreateVariants() {
        return getVariantsByType(ActivityModel.TYPE_ACTIVITY);
    }

    private List<ActivityCreateVariant> getVariantsByType(QName type) {

        List<ActivityCreateVariant> variants = new ArrayList<>();

        Collection<QName> subTypes = dictionaryService.getSubTypes(type, false);

        for (QName subType : subTypes) {

            if (subType.equals(type) || ignoredTypes.contains(subType)) {
                continue;
            }

            subType = subType.getPrefixedQName(namespaceService);

            NodeView viewParams = new NodeView.Builder(namespaceService)
                                              .mode(NodeViewMode.CREATE)
                                              .className(subType)
                                              .build();

            if (nodeViewService.hasNodeView(viewParams)) {

                TypeDefinition typeDef = dictionaryService.getType(subType);

                ActivityCreateVariant variant = new ActivityCreateVariant();
                variant.setType(subType);
                variant.setId(subType.toPrefixString());
                variant.setTitle(typeDef.getTitle(messageService));
                variant.setCanBeCreated(true);

                if (dictionaryService.isSubClass(subType, ICaseTaskModel.TYPE_TASK)) {
                    String workflow = getTaskWorkflowName(subType);
                    String titlePrefix = "";
                    if (StringUtils.isBlank(workflow) || workflow.startsWith(ActivitiConstants.ENGINE_ID)) {
                        titlePrefix = ACTIVITI_TASK_TITLE_PREFIX;
                    } else if (workflow.startsWith(FLOWABLE_ENGINE_PREFIX)) {
                        titlePrefix = FLOWABLE_TASK_TITLE_PREFIX;
                    }
                    variant.setTitle(titlePrefix + variant.getTitle());
                }

                variants.add(variant);
            }


            variants.addAll(getVariantsByType(subType));
        }
        return variants;
    }

    private String getTaskWorkflowName(QName taskType) {
        PropertyDefinition prop = dictionaryService.getProperty(taskType, ICaseTaskModel.PROP_WORKFLOW_DEFINITION_NAME);
        return prop.getDefaultValue();
    }

    public void setIgnoredTypes(Set<QName> ignoredTypes) {
        this.ignoredTypes = ignoredTypes;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        dictionaryService = serviceRegistry.getDictionaryService();
        namespaceService = serviceRegistry.getNamespaceService();
        messageService = serviceRegistry.getMessageService();
    }

    @Autowired
    public void setNodeViewService(NodeViewService nodeViewService) {
        this.nodeViewService = nodeViewService;
    }
}
