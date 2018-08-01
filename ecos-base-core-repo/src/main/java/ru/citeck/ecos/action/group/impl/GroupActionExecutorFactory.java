package ru.citeck.ecos.action.group.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupActionExecutorFactory implements GroupActionFactory {

    private static final String BATCH_PARAM_KEY = "evaluateBatch";

    private TransactionService transactionService;

    private GroupActionExecutor executor;

    public GroupActionExecutorFactory(GroupActionExecutor executor,
                                      TransactionService transactionService) {
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

    @Override
    public String[] getMandatoryParams() {
        return executor.getMandatoryParams();
    }

    private class BatchAction extends TxnGroupAction {

        BatchAction(GroupActionConfig config) {
            super(transactionService, config);
        }

        @Override
        protected boolean isApplicable(RemoteRef nodeRef) {
            return executor.isApplicable(nodeRef.getNodeRef(), config.getParams());
        }

        @Override
        protected void processNodesInTxn(List<RemoteRef> nodes, List<ActionResult> output) {

            List<NodeRef> nodeRefs = nodes.stream()
                    .filter(node -> {
                        boolean isApplicable = node.isLocal() && isApplicable(node);
                        if (!isApplicable) {
                            output.add(new ActionResult(node, ActionStatus.STATUS_SKIPPED));
                        }
                        return isApplicable;
                    })
                    .map(RemoteRef::getNodeRef)
                    .collect(Collectors.toList());

            Map<NodeRef, ActionStatus> results = executor.invokeBatch(nodeRefs, config.getParams());
            results.forEach((ref, res) -> output.add(new ActionResult(new RemoteRef(ref), res)));
        }
    }

    private class SimpleAction extends TxnGroupAction {

        SimpleAction(GroupActionConfig config) {
            super(transactionService, config);
        }

        @Override
        protected boolean isApplicable(RemoteRef nodeRef) {
            return executor.isApplicable(nodeRef.getNodeRef(), config.getParams());
        }

        @Override
        protected ActionStatus processImpl(RemoteRef nodeRef) {
            executor.invoke(nodeRef.getNodeRef(), config.getParams());
            return new ActionStatus(ActionStatus.STATUS_OK);
        }
    }
}
