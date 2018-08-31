package ru.citeck.ecos.action.group.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.source.alfnode.AlfNodesRecordsDAO;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GroupActionExecutorFactory implements GroupActionFactory<RecordRef> {

    private static final String BATCH_PARAM_KEY = "evaluateBatch";

    private TransactionService transactionService;

    private GroupActionExecutor executor;

    public GroupActionExecutorFactory(GroupActionExecutor executor,
                                      TransactionService transactionService) {
        this.executor = executor;
        this.transactionService = transactionService;
    }

    @Override
    public GroupAction<RecordRef> createAction(GroupActionConfig config) {
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

    private Optional<NodeRef> getNodeRef(RecordRef recordRef) {
        if (recordRef.getSourceId().isEmpty()) {
            return Optional.of(new NodeRef(recordRef.getId()));
        }
        return Optional.empty();
    }

    private class BatchAction extends TxnGroupAction<RecordRef> {

        BatchAction(GroupActionConfig config) {
            super(transactionService, config);
        }

        @Override
        protected boolean isApplicable(RecordRef recordRef) {
            return true;
        }

        @Override
        protected void processNodesInTxn(List<RecordRef> nodes, List<ActionResult<RecordRef>> output) {

            List<NodeRef> nodeRefs = nodes.stream()
                    .map(node -> new Pair<>(node, getNodeRef(node)))
                    .filter(nodePair -> {
                        NodeRef nodeRef = nodePair.getSecond().orElse(null);
                        boolean isApplicable = nodeRef != null && executor.isApplicable(nodeRef, config.getParams());
                        if (!isApplicable) {
                            output.add(new ActionResult<>(nodePair.getFirst(), ActionStatus.STATUS_SKIPPED));
                        }
                        return isApplicable;
                    })
                    .map(p -> p.getSecond().get())
                    .collect(Collectors.toList());

            Map<NodeRef, ActionStatus> results = executor.invokeBatch(nodeRefs, config.getParams());
            results.forEach((ref, res) -> output.add(new ActionResult<>(new RecordRef(ref.toString()), res)));
        }

        @Override
        public String toString() {
            return "BatchAction[" + executor + "]";
        }
    }


    private class SimpleAction extends TxnGroupAction<RecordRef> {

        SimpleAction(GroupActionConfig config) {
            super(transactionService, config);
        }

        @Override
        protected boolean isApplicable(RecordRef recordRef) {
            Optional<NodeRef> nodeRef = getNodeRef(recordRef);
            return nodeRef.isPresent() && executor.isApplicable(nodeRef.get(), config.getParams());
        }

        @Override
        protected ActionStatus processImpl(RecordRef recordRef) {
            Optional<NodeRef> nodeRef = getNodeRef(recordRef);
            nodeRef.ifPresent(nodeRef1 -> executor.invoke(nodeRef1, config.getParams()));
            return new ActionStatus(ActionStatus.STATUS_OK);
        }

        @Override
        public String toString() {
            return "SimpleAction[" + executor + "]";
        }
    }
}
