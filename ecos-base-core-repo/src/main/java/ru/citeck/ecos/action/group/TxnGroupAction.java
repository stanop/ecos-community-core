package ru.citeck.ecos.action.group;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    protected void processNodesImpl(List<ActionNode> nodes) {

        List<ActionNode> nodesToProcess = new ArrayList<>(nodes);

        boolean completed = false;

        while (!completed && nodesToProcess.size() > 0) {

            try {

                transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                    processNodesInTxn(nodesToProcess);
                    return null;
                }, false, true);

                completed = true;

            } catch (TxnException e) {

                GroupActionResult result = new GroupActionResult();
                result.setStatus(GroupActionResult.STATUS_ERROR);
                result.setException(e.cause);

                ActionNode node = nodesToProcess.get(e.nodeIdx);
                node.setResult(result);

                logger.warn("Exception while process node " + node.getNodeRef(), e.cause);

                nodesToProcess.remove(e.nodeIdx);

            } catch (Exception e) {

                GroupActionResult result = new GroupActionResult();
                result.setStatus(GroupActionResult.STATUS_ERROR);
                result.setException(e);

                nodesToProcess.forEach(n -> n.setResult(result));
                nodesToProcess.clear();

                logger.warn("Exception while process nodes " + nodesToProcess, e);
            }
        }
    }

    protected void processNodesInTxn(List<ActionNode> nodes) {
        for (int idx = 0; idx < nodes.size(); idx++) {
            try {
                ActionNode node = nodes.get(idx);
                if (isApplicable(node.getNodeRef())) {
                    node.process(this::processImpl);
                } else {
                    node.setResult(new GroupActionResult(GroupActionResult.STATUS_SKIPPED));
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
    protected GroupActionResult processImpl(RemoteRef nodeRef) {
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
