package ru.citeck.ecos.workflow.variable.type;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.Collection;

public class NodeRefsList extends ArrayList<NodeRef> implements EcosPojoType {

    public NodeRefsList() {
    }

    public NodeRefsList(Collection<NodeRef> other) {
        super(other);
    }
}
