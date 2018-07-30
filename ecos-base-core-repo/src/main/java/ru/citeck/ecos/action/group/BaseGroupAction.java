package ru.citeck.ecos.action.group;

import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author Pavel Simonov
 */
public abstract class BaseGroupAction implements GroupAction {

    private List<ActionNode> nodes = new ArrayList<>();

    private int batchSize;

    protected final GroupActionConfig config;

    public BaseGroupAction(GroupActionConfig config) {
        this.config = config;
        batchSize = config != null ? config.getBatchSize() : 1;
    }

    @Override
    public Future<GroupActionResult> process(RemoteNodeRef nodeRef) {
        ActionNode node = new ActionNode(nodeRef);
        nodes.add(node);
        if (batchSize > 0 && nodes.size() >= batchSize) {
            processNodes();
        }
        return node;
    }

    @Override
    public void end() {
        if (nodes.size() > 0) {
            processNodes();
        }
    }

    private void processNodes() {
        processNodesImpl(nodes);
        nodes.clear();
    }

    protected void processNodesImpl(List<ActionNode> nodes) {
        for (ActionNode node : nodes) {
            node.process(this::processImpl);
        }
    }

    protected GroupActionResult processImpl(RemoteNodeRef nodeRef) {
        throw new RuntimeException("Method not implemented");
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public static class ActionNode implements Future<GroupActionResult> {

        private final RemoteNodeRef nodeRef;
        private GroupActionResult result;

        ActionNode(RemoteNodeRef nodeRef) {
            this.nodeRef = nodeRef;
        }

        public RemoteNodeRef getNodeRef() {
            return nodeRef;
        }

        public void process(Function<RemoteNodeRef, GroupActionResult> processor) {
            result = processor.apply(nodeRef);
        }

        public void setResult(GroupActionResult result) {
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public GroupActionResult get() throws InterruptedException, ExecutionException {
            return result;
        }

        @Override
        public GroupActionResult get(long timeout, TimeUnit unit) {
            return result;
        }
    }
}
