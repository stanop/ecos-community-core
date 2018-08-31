package ru.citeck.ecos.records.action;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.action.group.impl.NodeRefActionFactory;
import ru.citeck.ecos.action.group.impl.TxnGroupAction;

@Component
public class TestGroupAction extends NodeRefActionFactory {

    private static final Log logger = LogFactory.getLog(TestGroupAction.class);

    private TransactionService transactionService;

    @Autowired
    public TestGroupAction(GroupActionService groupActionService,
                           TransactionService transactionService) {
        this.transactionService = transactionService;
        groupActionService.register(this);
    }

    @Override
    protected GroupAction<NodeRef> createNodeRefAction(GroupActionConfig config) {
        return new TestAct(transactionService, config);
    }

    @Override
    public String getActionId() {
        return "TEST_ACTION";
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    class TestAct extends TxnGroupAction<NodeRef> {

        public TestAct(TransactionService transactionService, GroupActionConfig config) {
            super(transactionService, config);
        }

        @Override
        protected ActionStatus processImpl(NodeRef nodeRef) {
            logger.error("ACTION UPON " + nodeRef);
            return new ActionStatus(ActionStatus.STATUS_OK);
        }
    }
}
