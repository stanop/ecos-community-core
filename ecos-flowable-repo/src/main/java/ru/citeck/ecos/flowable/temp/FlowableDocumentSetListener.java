package ru.citeck.ecos.flowable.temp;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;

/**
 * Created by impi on 13.10.17.
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
//
//    @Override
//    public void notify(DelegateExecution delegateExecution) {
//
//    }
}
