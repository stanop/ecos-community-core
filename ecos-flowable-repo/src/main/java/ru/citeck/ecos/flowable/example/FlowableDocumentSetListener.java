package ru.citeck.ecos.flowable.example;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;

/**
 * Document set listener
 */
public class FlowableDocumentSetListener extends AbstractExecutionListener {

    private static final String VAR_DOCUMENT = "document";
    private NodeService nodeService;

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
    }

    @Override
    protected void notifyImpl(DelegateExecution execution) {
        NodeRef document = FlowableListenerUtils.getDocument(execution, nodeService);
        if(document != null) {
            execution.setVariable(VAR_DOCUMENT, document);
        } else {
            execution.setVariable(VAR_DOCUMENT, null);
        }
    }
}
