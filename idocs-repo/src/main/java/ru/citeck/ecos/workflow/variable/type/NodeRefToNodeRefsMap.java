package ru.citeck.ecos.workflow.variable.type;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.HashMap;
import java.util.List;

public class NodeRefToNodeRefsMap extends HashMap<NodeRef, List<NodeRef>> implements EcosPojoType {
}
