package ru.citeck.ecos.journals.action.group;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Pavel Simonov
 */
public class TxnGroupActionProcessor extends BaseGroupActionProcessor {

    private static final List<Class> RETRY_EXCEPTIONS = Arrays.asList(RetryingTransactionHelper.RETRY_EXCEPTIONS);

    private static final Log logger = LogFactory.getLog(TxnGroupActionProcessor.class);

    private TransactionService transactionService;

    public TxnGroupActionProcessor(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    protected void processNodesImpl(List<ProcessorNode> nodes) {

        List<ProcessorNode> nodesToProcess = new ArrayList<>(nodes);

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
                result.setException(e.originalException);

                ProcessorNode node = nodesToProcess.get(e.nodeIdx);
                node.setResult(result);

                logger.warn("Exception while process node " + node.getNodeRef(), e.originalException);

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

    protected void processNodesInTxn(List<ProcessorNode> nodes) {
        for (int idx = 0; idx < nodes.size(); idx++) {
            try {
                ProcessorNode node = nodes.get(idx);
                if (isApplicable(node.getNodeRef())) {
                    node.process(this::processImpl);
                } else {
                    GroupActionResult result = new GroupActionResult();
                    result.setStatus(GroupActionResult.STATUS_SKIPPED);
                    node.setResult(result);
                }
            } catch (Exception e) {
                if (!RETRY_EXCEPTIONS.contains(e.getClass())) {
                    throw new TxnException(idx, e);
                } else {
                    throw e;
                }
            }
        }
    }

    protected boolean isApplicable(RemoteNodeRef nodeRef) {
        return true;
    }

    @Override
    protected GroupActionResult processImpl(RemoteNodeRef nodeRef) {
        throw new RuntimeException("Method not implemented");
    }

    protected static class TxnException extends RuntimeException {

        final int nodeIdx;
        final Exception originalException;

        TxnException(int idx, Exception originalException) {
            this.nodeIdx = idx;
            this.originalException = originalException;
        }
    }
}
