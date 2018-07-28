package ru.citeck.ecos.journals.action;

import ru.citeck.ecos.journals.action.group.GroupActionResult;
import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author Pavel Simonov
 */
public class BaseActionProcessor implements GroupActionProcessor {

    private int batchSize = 1;

    /*private List<RemoteNodeRef, FutureTask<GroupActionResult>> resultsFuture = new ArrayList<>();
    private List<RemoteNodeRef> nodesToProcess = new ArrayList<>();
    private Map<RemoteNodeRef, GroupActionResult> results = new HashMap<>();*/

    private List<ActionTask> tasks = new ArrayList<>();

    private Map<String, String> contextParams = Collections.emptyMap();

    public BaseActionProcessor() {
    }

    public BaseActionProcessor(Map<String, String> params) {
        this.contextParams = params;
    }

    @Override
    public Future<GroupActionResult> process(RemoteNodeRef nodeRef) {
        FutureTask<GroupActionResult> result = new FutureTask<>(() -> results.get(nodeRef));
        resultsFuture.add(result);
        nodesToProcess.add(nodeRef);
        if (batchSize > 0 && nodesToProcess.size() >= batchSize) {
            processNodes();
        }
        return result;
    }

    protected Map<RemoteNodeRef, GroupActionResult> processListImpl(List<RemoteNodeRef> nodes) {
        for (RemoteNodeRef node : nodes) {
            results.put(node, processImpl(node));
        }
    }

    protected GroupActionResult processImpl(RemoteNodeRef nodeRef) {
        return new GroupActionResult(GroupActionResult.STATUS_SKIPPED);
    }

    protected boolean isApplicable(RemoteNodeRef nodeRef) {
        return true;
    }

    @Override
    public void end() {
        if (nodesToProcess.size() > 0) {
            processNodes();
        }
    }

    private void processActions() {


        /*processListImpl(nodesToProcess);
        nodesToProcess.clear();
        for (FutureTask<GroupActionResult> task : resultsFuture) {
            task.run();
        }
        resultsFuture.clear();*/
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    private static class ActionTask {

        private final RemoteNodeRef nodeRef;
        private final FutureTask<GroupActionResult> futureResult;

        GroupActionResult result;

        public ActionTask(RemoteNodeRef nodeRef) {
            this.nodeRef = nodeRef;
            this.futureResult = new FutureTask<>(this::getResult);
        }

        private GroupActionResult getResult() {
            return result;
        }
    }
}
