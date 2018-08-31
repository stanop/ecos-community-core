package ru.citeck.ecos.action.group.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.action.group.*;

import java.util.*;
import java.util.stream.Collectors;

public class GroupActionExecutorFactory extends NodeRefActionFactory {

    private static final String BATCH_PARAM_KEY = "evaluateBatch";

    private TransactionService transactionService;

    private GroupActionExecutor executor;

    public GroupActionExecutorFactory(GroupActionExecutor executor,
                                      TransactionService transactionService) {
        this.executor = executor;
        this.transactionService = transactionService;
    }

    @Override
    protected GroupAction<NodeRef> createNodeRefAction(GroupActionConfig config) {
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

    private Map<String, String> getPlainParams(ObjectNode node) {
        Map<String, String> plainParams = new HashMap<>();
        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            plainParams.put(name, node.get(name).asText());
        }
        return plainParams;
    }

    private class BatchAction extends TxnGroupAction<NodeRef> {

        Map<String, String> plainParams;

        BatchAction(GroupActionConfig config) {
            super(transactionService, config);
            plainParams = getPlainParams(config.getParams());
        }

        @Override
        protected boolean isApplicable(NodeRef recordRef) {
            return executor.isApplicable(recordRef, plainParams);
        }

        @Override
        protected void processNodesInTxn(List<NodeRef> nodes, List<ActionResult<NodeRef>> output) {

            List<NodeRef> nodeRefs = nodes.stream()
                    .filter(nodeRef -> {
                        boolean isApplicable = nodeRef != null && executor.isApplicable(nodeRef, plainParams);
                        if (!isApplicable(nodeRef)) {
                            output.add(new ActionResult<>(nodeRef, ActionStatus.STATUS_SKIPPED));
                        }
                        return isApplicable;
                    })
                    .collect(Collectors.toList());

            Map<NodeRef, ActionStatus> results = executor.invokeBatch(nodeRefs, plainParams);
            results.forEach((ref, res) -> output.add(new ActionResult<>(ref, res)));
        }

        @Override
        public String toString() {
            return "BatchAction[" + executor + "]";
        }
    }

    private class SimpleAction extends TxnGroupAction<NodeRef> {

        private Map<String, String> plainParams;

        SimpleAction(GroupActionConfig config) {
            super(transactionService, config);
            plainParams = getPlainParams(config.getParams());
        }

        @Override
        protected boolean isApplicable(NodeRef nodeRef) {
            return executor.isApplicable(nodeRef, plainParams);
        }

        @Override
        protected ActionStatus processImpl(NodeRef nodeRef) {
            executor.invoke(nodeRef, plainParams);
            return new ActionStatus(ActionStatus.STATUS_OK);
        }

        @Override
        public String toString() {
            return "SimpleAction[" + executor + "]";
        }
    }
}
