package ru.citeck.ecos.utils.json.typed;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Pavel Simonov
 */
public class NodeRefList extends ArrayList<NodeRef> {

    public NodeRefList(int initialCapacity) {
        super(initialCapacity);
    }

    public NodeRefList() {
    }

    public NodeRefList(Collection<? extends NodeRef> c) {
        super(c);
    }
}
