package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

/**
 * @author Roman Makarskiy
 */
public class FlowableModelerServiceJS extends AlfrescoScopableProcessorExtension {

    private FlowableModelerService flowableModelerService;

    public void importProcessModel(Object nodeRef) {
        NodeRef workflowRef = JavaScriptImplUtils.getNodeRef(nodeRef);
        flowableModelerService.importProcessModel(workflowRef);
    }

    public void importProcessModel() {
        flowableModelerService.importProcessModel();
    }

    public void setFlowableModelerService(FlowableModelerService flowableModelerService) {
        this.flowableModelerService = flowableModelerService;
    }
}
