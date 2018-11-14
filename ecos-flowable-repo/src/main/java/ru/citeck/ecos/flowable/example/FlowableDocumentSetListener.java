package ru.citeck.ecos.flowable.example;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.flowable.variable.FlowableActivitiScriptNode;

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
        if (document != null) {

            Context context = Context.enter();
            Scriptable scope = context.initStandardObjects();

            FlowableActivitiScriptNode node;
            try {
                node = new FlowableActivitiScriptNode(document, serviceRegistry, scope);
            } finally {
                Context.exit();
            }

            execution.setVariable(VAR_DOCUMENT, node);
        } else {
            execution.setVariable(VAR_DOCUMENT, null);
        }
    }
}
