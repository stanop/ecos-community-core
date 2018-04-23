package ru.citeck.ecos.behavior.common;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import ru.citeck.ecos.model.CounterModel;

import java.util.LinkedList;
import java.util.List;

public class SimpleSplit implements SplitChildrenBehaviour.SplitBehaviour {
    private NodeService nodeService;
    private int childrenPerParent = 500;
    private int depth = 1;

    public SimpleSplit() {
    }

    @Override
    public void init(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
    }

    @Override
    public List<String> getPath(NodeRef parent, NodeRef node) {

        int counter = getCounterValue(parent);
        int base = counter / childrenPerParent;

        List<String> path = new LinkedList<>();

        for (int i = 0; i < depth; i++) {
            path.add(0, String.valueOf(base % childrenPerParent));
            base /= childrenPerParent;
        }

        return path;
    }

    @Override
    public void onSuccess(NodeRef parent, NodeRef node) {

    }

    private synchronized int getCounterValue(NodeRef parent) {
        Integer counterValue = (Integer) nodeService.getProperty(parent, CounterModel.PROP_CHILDREN_COUNT_VALUE);
        if (counterValue == null) {
            counterValue = 0;
        }
        counterValue++;
        nodeService.setProperty(parent, CounterModel.PROP_CHILDREN_COUNT_VALUE, counterValue);
        return counterValue;
    }

    public void setChildrenPerParent(int childrenPerParent) {
        if (childrenPerParent <= 0) {
            throw new IllegalArgumentException("Children count must be greater than zero");
        }
        this.childrenPerParent = childrenPerParent;
    }

    public void setDepth(int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Depth must be greater than zero");
        }
        this.depth = depth;
    }
}
