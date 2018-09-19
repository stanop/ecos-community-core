package ru.citeck.ecos.action.group.impl;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Simonov
 */
public abstract class TxnGroupAction<T> extends BaseGroupAction<T> {

    private static final Log logger = LogFactory.getLog(TxnGroupAction.class);

    private TransactionService transactionService;

    public TxnGroupAction(TransactionService transactionService, GroupActionConfig config) {
        super(config);
        this.transactionService = transactionService;
    }

    @Override
    protected final void processNodesImpl(List<T> nodes) {

        List<T> nodesToProcess = new ArrayList<>(nodes);

        List<ActionResult<T>> transactionResults = new ArrayList<>();

        while (nodesToProcess.size() > 0) {

            try {

                RetryingTransactionHelper tHelper = transactionService.getRetryingTransactionHelper();
                List<ActionResult<T>> txnOutput = tHelper.doInTransaction(() -> processNodesInTxn(nodesToProcess),
                                                                          false, true);
                transactionResults.addAll(txnOutput);
                nodesToProcess.clear();

            } catch (TxnException e) {

                ActionStatus status = new ActionStatus();
                status.setKey(ActionStatus.STATUS_ERROR);
                status.setException(e.cause);

                T node = nodesToProcess.get(e.nodeIdx);
                transactionResults.add(new ActionResult<>(node, status));

                logger.error("Exception while process node " + node +
                             " action: " + toString() + " config: " + config, e.cause);

                nodesToProcess.remove(e.nodeIdx);

            } catch (Exception e) {

                ActionStatus status = new ActionStatus();
                status.setKey(ActionStatus.STATUS_ERROR);
                status.setException(e);

                nodesToProcess.forEach(n -> transactionResults.add(new ActionResult<>(n, status)));
                nodesToProcess.clear();

                logger.error("Exception while process nodes " + nodesToProcess +
                             " action: " + toString() + " config: " + config, e);
            }
        }

        onProcessed(transactionResults);
    }

    @Override
    public final void onProcessed(List<ActionResult<T>> actionResults) {
        super.onProcessed(actionResults);
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            onProcessedInTxn(actionResults);
            return null;
        });
    }

    protected void onProcessedInTxn(List<ActionResult<T>> results) {
    }

    protected List<ActionResult<T>> processNodesInTxn(List<T> nodes) {

        List<ActionResult<T>> output = new ArrayList<>();

        for (int idx = 0; idx < nodes.size(); idx++) {
            try {
                T node = nodes.get(idx);
                if (isApplicable(node)) {
                    ActionStatus status = processImpl(node);
                    output.add(new ActionResult<>(node, status));
                } else {
                    output.add(new ActionResult<>(node, ActionStatus.STATUS_SKIPPED));
                }
            } catch (Exception e) {
                Throwable retryCause = RetryingTransactionHelper.extractRetryCause(e);
                if (retryCause == null) {
                    throw new TxnException(idx, e);
                } else {
                    throw e;
                }
            }
        }

        return output;
    }

    protected boolean isApplicable(T nodeId) {
        return true;
    }

    @Override
    protected ActionStatus processImpl(T nodeRef) {
        throw new RuntimeException("Method not implemented");
    }

    protected static class TxnException extends RuntimeException {

        final int nodeIdx;
        final Exception cause;

        TxnException(int idx, Exception cause) {
            this.nodeIdx = idx;
            this.cause = cause;
        }
    }
}
