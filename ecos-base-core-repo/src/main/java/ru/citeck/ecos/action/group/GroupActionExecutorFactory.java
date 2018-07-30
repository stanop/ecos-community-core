package ru.citeck.ecos.action.group;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupActionExecutorFactory implements GroupActionFactory {

    private static final String BATCH_PARAM_KEY = "evaluateBatch";

    private TransactionService transactionService;

    private GroupActionExecutor executor;

    GroupActionExecutorFactory(GroupActionExecutor executor, TransactionService transactionService) {
        this.executor = executor;
        this.transactionService = transactionService;
    }

    @Override
    public GroupAction createAction(GroupActionConfig config) {
        GroupActionConfig actionConfig = config;
        boolean isBatch = Boolean.TRUE.toString().equals(config.getParams().get(BATCH_PARAM_KEY));
        if (isBatch) {
            actionConfig = new GroupActionConfig(actionConfig);
            actionConfig.setBatchSize(0);
            return new BatchAction(actionConfig);
        } else {
            return new SimpleAction(actionConfig);
        }
    }

    @Override
    public String getActionId() {
        return executor.getActionId();
    }

    private class BatchAction extends TxnGroupAction {

        BatchAction(GroupActionConfig config) {
            super(transactionService, config);
        }

        @Override
        protected boolean isApplicable(RemoteNodeRef nodeRef) {
            return executor.isApplicable(nodeRef.getNodeRef(), config.getParams());
        }

        @Override
        protected void processNodesInTxn(List<ActionNode> nodes) {

            Map<NodeRef, ActionNode> actionNodesByNodeRef = new HashMap<>();
            List<NodeRef> nodeRefs = nodes.stream()
                    .filter(node -> {
                        boolean isApplicable = isApplicable(node.getNodeRef());
                        if (!isApplicable) {
                            node.setResult(new GroupActionResult(GroupActionResult.STATUS_SKIPPED));
                        }
                        return isApplicable;
                    })
                    .map(node -> {
                                NodeRef nodeRef = node.getNodeRef().getNodeRef();
                                actionNodesByNodeRef.put(nodeRef, node);
                                return nodeRef;
                            }
                    ).collect(Collectors.toList());

            Map<NodeRef, GroupActionResult> results = executor.invokeBatch(nodeRefs, config.getParams());
            actionNodesByNodeRef.forEach((ref, node) -> node.setResult(results.get(ref)));
        }
    }

    private class SimpleAction extends TxnGroupAction {

        SimpleAction(GroupActionConfig config) {
            super(transactionService, config);
        }

        @Override
        protected boolean isApplicable(RemoteNodeRef nodeRef) {
            return executor.isApplicable(nodeRef.getNodeRef(), config.getParams());
        }

        @Override
        protected GroupActionResult processImpl(RemoteNodeRef nodeRef) {
            executor.invoke(nodeRef.getNodeRef(), config.getParams());
            return new GroupActionResult(GroupActionResult.STATUS_OK);
        }
    }
}
