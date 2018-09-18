package ru.citeck.ecos.action.group.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.records.RecordInfo;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.actions.RecordsActionFactory;
import ru.citeck.ecos.records.actions.RecordsGroupAction;

import java.util.*;
import java.util.stream.Collectors;

public class GroupActionExecutorFactory extends RecordsActionFactory<NodeRef> {

    private static final String BATCH_PARAM_KEY = "evaluateBatch";

    private TransactionService transactionService;

    private GroupActionExecutor executor;

    public GroupActionExecutorFactory(GroupActionExecutor executor,
                                      TransactionService transactionService) {
        this.executor = executor;
        this.transactionService = transactionService;
    }

    @Override
    protected RecordsGroupAction<NodeRef> createLocalAction(GroupActionConfig config) {
        GroupActionConfig actionConfig = new GroupActionConfig(config);
        JsonNode batchParamNode = config.getParams().get(BATCH_PARAM_KEY);
        boolean isBatch = batchParamNode != null && Boolean.TRUE.toString().equals(batchParamNode.asText());
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

    private class BatchAction extends TxnGroupAction<RecordInfo<NodeRef>>
                              implements RecordsGroupAction<NodeRef> {

        Map<String, String> plainParams;

        BatchAction(GroupActionConfig config) {
            super(transactionService, config);
            plainParams = getPlainParams(config.getParams());
        }

        @Override
        protected boolean isApplicable(RecordInfo<NodeRef> recordInfo) {
            return executor.isApplicable(recordInfo.getData(), plainParams);
        }

        @Override
        protected List<ActionResult<RecordInfo<NodeRef>>> processNodesInTxn(List<RecordInfo<NodeRef>> nodes) {

            List<ActionResult<RecordInfo<NodeRef>>> output = new ArrayList<>();
            Map<NodeRef, RecordInfo<NodeRef>> recordsMapping = new HashMap<>();

            List<NodeRef> nodeRefs = nodes.stream()
                    .filter(nodeInfo -> {
                        if (!isApplicable(nodeInfo)) {
                            output.add(new ActionResult<>(nodeInfo, ActionStatus.STATUS_SKIPPED));
                            return false;
                        }
                        return true;
                    }).map(nodeInfo -> {
                        recordsMapping.put(nodeInfo.getData(), nodeInfo);
                        return nodeInfo.getData();
                    })
                    .collect(Collectors.toList());

            Map<NodeRef, ActionStatus> results = executor.invokeBatch(nodeRefs, plainParams);

            results.forEach((ref, res) -> {
                RecordInfo<NodeRef> info = recordsMapping.get(ref);
                if (info == null) {
                    info = new RecordInfo<>(new RecordRef(ref), ref);
                }
                output.add(new ActionResult<>(info, res));
            });

            return output;
        }

        @Override
        public String toString() {
            return "BatchAction[" + executor + "]";
        }
    }

    private class SimpleAction extends TxnGroupAction<RecordInfo<NodeRef>>
                               implements RecordsGroupAction<NodeRef> {

        private Map<String, String> plainParams;

        SimpleAction(GroupActionConfig config) {
            super(transactionService, config);
            plainParams = getPlainParams(config.getParams());
        }

        @Override
        protected boolean isApplicable(RecordInfo<NodeRef> nodeInfo) {
            return executor.isApplicable(nodeInfo.getData(), plainParams);
        }

        @Override
        protected ActionStatus processImpl(RecordInfo<NodeRef> nodeInfo) {
            executor.invoke(nodeInfo.getData(), plainParams);
            return new ActionStatus(ActionStatus.STATUS_OK);
        }

        @Override
        public String toString() {
            return "SimpleAction[" + executor + "]";
        }
    }
}
