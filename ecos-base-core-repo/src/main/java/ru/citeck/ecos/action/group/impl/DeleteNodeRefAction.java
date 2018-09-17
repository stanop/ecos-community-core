package ru.citeck.ecos.action.group.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.records.RecordInfo;
import ru.citeck.ecos.records.actions.RecordsActionFactory;
import ru.citeck.ecos.records.actions.RecordsGroupAction;

/**
 * @author Pavel Simonov
 */
@Component
public class DeleteNodeRefAction extends RecordsActionFactory<NodeRef> {

    private static final Log logger = LogFactory.getLog(DeleteNodeRefAction.class);

    public static final String ACTION_ID = "node-service-delete-node";

    private NodeService nodeService;
    private TransactionService transactionService;

    @Autowired
    public DeleteNodeRefAction(NodeService nodeService,
                               TransactionService transactionService,
                               GroupActionService groupActionService) {
        this.nodeService = nodeService;
        this.transactionService = transactionService;
        groupActionService.register(this);
    }

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    protected RecordsGroupAction<NodeRef> createLocalAction(GroupActionConfig config) {
        return new Action(config);
    }

    class Action extends TxnGroupAction<RecordInfo<NodeRef>> implements RecordsGroupAction<NodeRef> {

        Action(GroupActionConfig config) {
            super(transactionService, config);
        }

        @Override
        protected ActionStatus processImpl(RecordInfo<NodeRef> nodeRef) {
            nodeService.deleteNode(nodeRef.getData());
            return new ActionStatus(ActionStatus.STATUS_OK);
        }
    }
}
