package ru.citeck.ecos.journals.group;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceImpl implements GroupActionService {

    private static Log logger = LogFactory.getLog(GroupActionServiceImpl.class);

    @Autowired
    private TransactionService transactionService;

    private Map<String, GroupActionEvaluator> evaluators = new HashMap<>();

    @Override
    public Map<NodeRef, GroupActionStatus> invoke(List<NodeRef> nodeRefs, String actionId, Map<String, String> params) {

        GroupActionEvaluator evaluator = evaluators.get(actionId);
        if (evaluator == null) {
            throw new IllegalArgumentException("Action not found: '" + actionId + "'");
        }
        checkParams(params, evaluator.getMandatoryParams());

        Map<NodeRef, GroupActionStatus> statuses = new HashMap<>();

        for (NodeRef ref : nodeRefs) {
            if (evaluator.isApplicable(ref, params)) {
                statuses.put(ref, processNode(ref, evaluator, params));
            } else {
                statuses.put(ref, new GroupActionStatus(GroupActionStatus.STATUS_SKIPPED));
            }
        }

        return statuses;
    }

    @Override
    public Map<NodeRef, GroupActionStatus> invokeBatch(List<NodeRef> nodeRefs, String actionId, Map<String, String> params) {

        GroupActionEvaluator evaluator = evaluators.get(actionId);
        if (evaluator == null) {
            throw new IllegalArgumentException("Action not found: '" + actionId + "'");
        }
        checkParams(params, evaluator.getMandatoryParams());

        Map<NodeRef, GroupActionStatus> statuses = evaluator.invokeBatch(nodeRefs, params);

        return statuses;
    }

    private GroupActionStatus processNode(NodeRef nodeRef, GroupActionEvaluator evaluator, Map<String, String> params) {

        final GroupActionStatus status = new GroupActionStatus();

        try {
            transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                evaluator.invoke(nodeRef, params);
                return null;
            }, false, true);
        } catch (Exception e) {
            status.setStatus(GroupActionStatus.STATUS_ERROR);
            status.setException(e);
            logger.error("Error while node processing", e);
        }
        return status;
    }

    private GroupActionStatus processNodes(List<NodeRef> nodeRefs, GroupActionEvaluator evaluator, Map<String, String> params) {

        final GroupActionStatus status = new GroupActionStatus();

        try {
            transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                evaluator.invokeBatch(nodeRefs, params);
                return null;
            }, false, true);
        } catch (Exception e) {
            status.setStatus(GroupActionStatus.STATUS_ERROR);
            status.setException(e);
            logger.error("Error while node processing", e);
        }
        return status;
    }



    private void checkParams(Map<String, String> params, String[] mandatoryParams) {
        List<String> missing = new ArrayList<>(mandatoryParams.length);
        for (String param : mandatoryParams) {
            if (!params.containsKey(param) || StringUtils.isBlank(params.get(param))) {
                missing.add(param);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Mandatory parameters are missing: " + String.join(", ", missing));
        }
    }

    @Override
    public void register(GroupActionEvaluator evaluator) {
        evaluators.put(evaluator.getActionId(), evaluator);
    }
}
