package ru.citeck.ecos.journals.action.group;

import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author Pavel Simonov
 */
public class BaseGroupActionProcessor implements GroupActionProcessor {

    private int batchSize = 1;

    private List<ProcessorNode> nodes = new ArrayList<>();

    @Override
    public Future<GroupActionResult> process(RemoteNodeRef nodeRef) {
        ProcessorNode node = new ProcessorNode(nodeRef);
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

    protected void processNodesImpl(List<ProcessorNode> nodes) {
        for (ProcessorNode node : nodes) {
            node.process(this::processImpl);
        }
    }

    protected GroupActionResult processImpl(RemoteNodeRef nodeRef) {
        throw new RuntimeException("Method not implemented");
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public static class ProcessorNode implements Future<GroupActionResult> {

        private final RemoteNodeRef nodeRef;
        private GroupActionResult result;

        ProcessorNode(RemoteNodeRef nodeRef) {
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
