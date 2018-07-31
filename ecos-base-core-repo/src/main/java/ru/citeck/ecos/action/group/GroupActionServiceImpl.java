package ru.citeck.ecos.action.group;

import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Pavel Simonov
 */
public class GroupActionServiceImpl implements GroupActionService {

    private static final long PROCESS_TIMEOUT_SEC = 60 * 5;

    private TransactionService transactionService;

    private Map<String, GroupActionFactory> processorFactories = new HashMap<>();

    @Autowired
    public GroupActionServiceImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Map<RemoteRef, GroupActionResult> execute(Iterable<RemoteRef> nodeRefs,
                                                     GroupAction action) {

        Map<RemoteRef, Future<GroupActionResult>> futureResults = new HashMap<>();

        for (RemoteRef ref : nodeRefs) {
            futureResults.put(ref, action.process(ref));
        }

        Map<RemoteRef, GroupActionResult> results = new HashMap<>();

        futureResults.forEach((ref, res) -> {

            GroupActionResult result;
            try {
                result = res.get(PROCESS_TIMEOUT_SEC, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException("Processing result get timeout exception. " +
                                           "Time: " + PROCESS_TIMEOUT_SEC + " seconds. " +
                                           "Node: " + ref, e);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Exception while processing result get. Node: " + ref, e);
            }
            results.put(ref, result);
        });

        return results;
    }

    @Override
    public void executeAsync(Iterable<RemoteRef> nodeRefs,
                             GroupAction action) {

        for (RemoteRef ref : nodeRefs) {
            action.process(ref);
        }
    }

    @Override
    public Map<RemoteRef, GroupActionResult> execute(Iterable<RemoteRef> nodeRefs,
                                                     Consumer<RemoteRef> action,
                                                     GroupActionConfig config) {

        return execute(nodeRefs, new CustomTxnGroupAction(transactionService, action, config));
    }

    @Override
    public void executeAsync(Iterable<RemoteRef> nodeRefs,
                             Consumer<RemoteRef> action,
                             GroupActionConfig config) {
        executeAsync(nodeRefs, new CustomTxnGroupAction(transactionService, action, config));
    }

    @Override
    public Map<RemoteRef, GroupActionResult> execute(Iterable<RemoteRef> nodeRefs,
                                                     Function<RemoteRef, GroupActionResult> action,
                                                     GroupActionConfig config) {

        return execute(nodeRefs, new CustomTxnGroupAction(transactionService, action, config));
    }

    @Override
    public void executeAsync(Iterable<RemoteRef> nodeRefs,
                             Function<RemoteRef, GroupActionResult> action,
                             GroupActionConfig config) {
        executeAsync(nodeRefs, new CustomTxnGroupAction(transactionService, action, config));
    }

    @Override
    public Map<RemoteRef, GroupActionResult> execute(Iterable<RemoteRef> nodeRefs,
                                                     String actionId,
                                                     GroupActionConfig config) {

        return execute(nodeRefs, getProcessor(actionId, config));
    }

    @Override
    public void executeAsync(Iterable<RemoteRef> nodeRefs,
                             String actionId,
                             GroupActionConfig config) {

        executeAsync(nodeRefs, getProcessor(actionId, config));
    }

    private GroupAction getProcessor(String actionId, GroupActionConfig config) {

        GroupActionFactory factory = processorFactories.get(actionId);
        if (factory == null) {
            throw new IllegalArgumentException("Action not found: '" + actionId + "'");
        }

        checkParams(config.getParams(), factory.getMandatoryParams());

        return factory.createAction(config);
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
    public void register(GroupActionFactory factory) {
        processorFactories.put(factory.getActionId(), factory);
    }

    @Override
    public void register(GroupActionExecutor executor) {
        register(new GroupActionExecutorFactory(executor, transactionService));
    }
}
