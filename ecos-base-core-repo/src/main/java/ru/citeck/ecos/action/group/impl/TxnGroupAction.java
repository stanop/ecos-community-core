package ru.citeck.ecos.action.group.impl;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Simonov
 */
public abstract class TxnGroupAction extends BaseGroupAction {

    private static final Log logger = LogFactory.getLog(TxnGroupAction.class);

    private TransactionService transactionService;

    public TxnGroupAction(TransactionService transactionService, GroupActionConfig config) {
        super(config);
        this.transactionService = transactionService;
    }

    @Override
    protected final void processNodesImpl(List<RemoteRef> nodes, List<ActionResult> output) {

        List<RemoteRef> nodesToProcess = new ArrayList<>(nodes);

        boolean completed = false;

        while (!completed && nodesToProcess.size() > 0) {

            try {

                List<ActionResult> txnOutput = new ArrayList<>();

                transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                    processNodesInTxn(nodesToProcess, txnOutput);
                    return null;
                }, false, true);

                for (ActionResult result : txnOutput) {
                    if (output.size() < config.getMaxResults()) {
                        output.add(result);
                    }
                }
                completed = true;

            } catch (TxnException e) {

                ActionStatus status = new ActionStatus();
                status.setKey(ActionStatus.STATUS_ERROR);
                status.setException(e.cause);

                RemoteRef node = nodesToProcess.get(e.nodeIdx);
                output.add(new ActionResult(node, status));

                logger.error("Exception while process node " + node +
                             " action: " + toString() + " config: " + config, e.cause);

                nodesToProcess.remove(e.nodeIdx);

            } catch (Exception e) {

                ActionStatus status = new ActionStatus();
                status.setKey(ActionStatus.STATUS_ERROR);
                status.setException(e);

                nodesToProcess.forEach(n -> output.add(new ActionResult(n, status)));
                nodesToProcess.clear();

                logger.error("Exception while process nodes " + nodesToProcess +
                             " action: " + toString() + " config: " + config, e);
            }
        }
    }

    protected void processNodesInTxn(List<RemoteRef> nodes, List<ActionResult> output) {
        for (int idx = 0; idx < nodes.size(); idx++) {
            try {
                RemoteRef node = nodes.get(idx);
                if (isApplicable(node)) {
                    ActionStatus status = processImpl(node);
                    output.add(new ActionResult(node, status));
                } else {
                    output.add(new ActionResult(node, ActionStatus.STATUS_SKIPPED));
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
    }

    protected boolean isApplicable(RemoteRef nodeRef) {
        return true;
    }

    @Override
    protected ActionStatus processImpl(RemoteRef nodeRef) {
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
