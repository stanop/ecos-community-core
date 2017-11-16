package ru.citeck.ecos.webscripts.cases;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.dto.ActivityVariantDto;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewMode;
import ru.citeck.ecos.invariants.view.NodeViewService;
import ru.citeck.ecos.template.TemplateNodeService;
import ru.citeck.ecos.utils.DictionaryUtils;

import java.util.*;

/**
 * Business process activities get web script
 */
public class BPActivitiesGet extends DeclarativeWebScript {

    /**
     * Constants
     */
    private static final String ACTIVITIES_VIEW_TYPE = "activ:activity";
    private static final String TYPE_NAMES = "viewTypeNames";
    private static final String VARIANTS = "variants";
    private static final String FLOWABLE_ENGINE_PREFIX = "flowable";
    private static final String WORKFLOW_DEFINITION_NAME = "workflowDefinitionName";
    private static final String WORKFLOW_FORM_KEY = "workflowFormKey";
    private static final String USER_TASK_TYPE = "iuserTask:task";
    private static final String USER_TASK_TITLE_KEY = "iuserTask_iuserTaskModel.type.iuserTask_task.title";

    private static final String USER_TASK_TYPES[] = {
            "iuserTask:task",
            "activ:activity",
            "dl:dataListItem",
            "cm:content",
            "cm:cmobject",
            "sys:base"
    };

    /**
     * Dictionary service
     */
    private DictionaryService dictionaryService;

    /**
     * Node view service
     */
    private NodeViewService nodeViewService;

    /**
     * Prefix resolver
     */
    private NamespacePrefixResolver prefixResolver;

    /**
     * Workflow service
     */
    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;

    /**
     * Template node service
     */
    @Autowired
    private TemplateNodeService templateNodeService;

    /**
     * Message service
     */
    @Autowired
    private MessageService messageService;

    /**
     * Execute implementation
     * @param req Request
     * @param status Status
     * @param cache Cache
     * @return Model map
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        /** Load flowable workflow definitions */
        List<WorkflowDefinition> definitions = workflowService.getDefinitions();
        List<WorkflowDefinition> actual = new ArrayList<>();
        for (WorkflowDefinition workflowDefinition : definitions) {
            if (workflowDefinition.getId().startsWith(FLOWABLE_ENGINE_PREFIX)) {
                actual.add(workflowDefinition);
            }
        }

        /** Load view classes and build actions */
        List<QName> viewClasses = getExistingViewClasses();
        Map<String, Object> result = new HashMap<>();
        result.put(TYPE_NAMES, createTypeNamesMap(viewClasses, actual));
        result.put(VARIANTS, createActivityVariants(viewClasses, actual));
        return result;
    }

    /**
     * Get existing view classes
     * @return List of existing view classes
     */
    private List<QName> getExistingViewClasses() {
        List<QName> existingViewClasses = new LinkedList<>();
        QName className = QName.resolveToQName(prefixResolver, ACTIVITIES_VIEW_TYPE);
        NodeView.Builder builder = new NodeView.Builder(prefixResolver).mode(NodeViewMode.CREATE);
        if(nodeViewExists(builder, className)) {
            existingViewClasses.add(className);
        }
        for(QName childClassName : DictionaryUtils.getChildClassNames(className, true, dictionaryService)) {
            if(childClassName.equals(className)) continue;
            if(nodeViewExists(builder, childClassName)) {
                existingViewClasses.add(childClassName);
            }
        }
        return existingViewClasses;
    }

    /**
     * Check node existing
     * @param builder Node view builder
     * @param className Class name
     * @return Check result
     */
    private boolean nodeViewExists(NodeView.Builder builder, QName className) {
        NodeView view = builder
                .className(className)
                .build();
        return nodeViewService.hasNodeView(view);
    }

    /**
     * Create activity variants data transfer object
     * @param viewClasses View classes
     * @param workflowDefinitions Workflow definition
     * @return List of variants
     */
    private List<ActivityVariantDto> createActivityVariants(List<QName> viewClasses, List<WorkflowDefinition> workflowDefinitions) {
        List<ActivityVariantDto> result = new ArrayList<>();
        /** View classes */
        for (QName typeQName : viewClasses) {
            ActivityVariantDto variantDto = new ActivityVariantDto();
            String title = templateNodeService.getClassTitle(typeQName.toPrefixString(prefixResolver));
            String type = typeQName.toPrefixString(prefixResolver);
            variantDto.setTitle(title);
            variantDto.setType(type);
            /** Parents */
            Collection<QName> parentTypes = templateNodeService.getParentClasses(type);
            List<String> parentTypeNames = new ArrayList<>();
            for (QName parentType : parentTypes) {
                parentTypeNames.add(parentType.toPrefixString(prefixResolver));
            }
            variantDto.setParentTypes(parentTypeNames);
            result.add(variantDto);
        }
        /** Workflow definition */
        for (WorkflowDefinition workflowDefinition : workflowDefinitions) {
            ActivityVariantDto variantDto = new ActivityVariantDto();
            variantDto.setType(workflowDefinition.getName());
            variantDto.setTitle(workflowDefinition.getTitle());
            variantDto.setParentTypes(Arrays.asList(USER_TASK_TYPES));
            Map<String, String> viewParams = new HashMap<>();
            viewParams.put(WORKFLOW_DEFINITION_NAME, workflowDefinition.getName());
            viewParams.put(WORKFLOW_FORM_KEY, workflowDefinition.getStartTaskDefinition().getId());
            variantDto.setViewParams(viewParams);
            result.add(variantDto);
        }
        return result;
    }

    /**
     * Create type name map
     * @param viewClasses View classes
     * @param workflowDefinitions Workflow definitions
     * @return Map of type names
     */
    private Map<String, String> createTypeNamesMap(List<QName> viewClasses, List<WorkflowDefinition> workflowDefinitions) {
        Map<String, String> result = new HashMap<>();
        /** View classes */
        for (QName typeQName : viewClasses) {
            String title = templateNodeService.getClassTitle(typeQName.toPrefixString(prefixResolver));
            result.put(typeQName.toPrefixString(prefixResolver), title);
            Collection<QName> parentTypes = templateNodeService.getParentClasses(typeQName.toPrefixString(prefixResolver));
            for (QName parentType : parentTypes) {
                String parentTitle = templateNodeService.getClassTitle(parentType.toPrefixString(prefixResolver));
                result.put(parentType.toPrefixString(prefixResolver), parentTitle);
            }
        }
        /** Workflow definition */
        for (WorkflowDefinition workflowDefinition : workflowDefinitions) {
            result.put(workflowDefinition.getName(), workflowDefinition.getTitle());
        }
        /** User tasks */
        result.put(USER_TASK_TYPE, messageService.getMessage(USER_TASK_TITLE_KEY));
        return result;
    }

    /** Setters */

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeViewService(NodeViewService nodeViewService) {
        this.nodeViewService = nodeViewService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }
}
