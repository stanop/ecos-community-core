package ru.citeck.ecos.action.group.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.records2.RecordRef;

import java.util.*;
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
        GroupActionConfig actionConfig = new GroupActionConfig(config);
        boolean isBatch = actionConfig.getBoolParam(BATCH_PARAM_KEY);
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

    private class BatchAction extends TxnGroupAction<RecordRef> {

        Map<String, String> plainParams;

        BatchAction(GroupActionConfig config) {
            super(transactionService, config);
            plainParams = getPlainParams(config.getParams());
        }

        @Override
        protected boolean isApplicable(RecordRef nodeRef) {
            return NodeRef.isNodeRef(nodeRef.getId()) &&
                   executor.isApplicable(new NodeRef(nodeRef.getId()), plainParams);
        }

        @Override
        protected List<ActionResult<RecordRef>> processNodesInTxn(List<RecordRef> nodes) {

            List<ActionResult<RecordRef>> output = new ArrayList<>();
            Map<NodeRef, RecordRef> recordsMapping = new HashMap<>();

            List<NodeRef> nodeRefs = nodes.stream()
                    .filter(recordRef -> {
                        if (!isApplicable(recordRef)) {
                            output.add(new ActionResult<>(recordRef, ActionStatus.STATUS_SKIPPED));
                            return false;
                        }
                        return true;
                    }).map(recordRef -> {
                        NodeRef nodeRef = new NodeRef(recordRef.getId());
                        recordsMapping.put(nodeRef, recordRef);
                        return nodeRef;
                    })
                    .collect(Collectors.toList());

            Map<NodeRef, ActionStatus> results = executor.invokeBatch(nodeRefs, plainParams);

            results.forEach((ref, res) -> {
                RecordRef recordRef = recordsMapping.get(ref);
                if (recordRef == null) {
                    recordRef = RecordRef.valueOf(ref.toString());
                }
                output.add(new ActionResult<>(recordRef, res));
            });

            return output;
        }

        @Override
        public String toString() {
            return "BatchAction[" + executor + "]";
        }
    }

    private class SimpleAction extends TxnGroupAction<RecordRef> {

        private Map<String, String> plainParams;

        SimpleAction(GroupActionConfig config) {
            super(transactionService, config);
            plainParams = getPlainParams(config.getParams());
        }

        @Override
        protected boolean isApplicable(RecordRef recordRef) {
            return NodeRef.isNodeRef(recordRef.getId()) &&
                    executor.isApplicable(new NodeRef(recordRef.getId()), plainParams);
        }

        @Override
        protected ActionStatus processImpl(RecordRef recordRef) {
            executor.invoke(new NodeRef(recordRef.getId()), plainParams);
            return new ActionStatus(ActionStatus.STATUS_OK);
        }

        @Override
        public String toString() {
            return "SimpleAction[" + executor + "]";
        }
    }
}
