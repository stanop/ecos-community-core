/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.ObjectUtils;

/**
 * Node info is an object, that contains some information about Alfresco node.
 * This node does not need to be existing - the NodeInfo object is virtual.
 * 
 * It can be used to represent information about the node, that is not yet created, 
 *  or that was deleted, or that never existed.
 * Equally, it can be used to represent information about currently existing node.
 * 
 * @author Sergey Tiunov
 *
 */
public class NodeInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private QName type;
	private List<QName> aspects;
	private NodeRef nodeRef;
	private NodeRef parent;
	private QName parentAssoc;
	private Map<QName,Serializable> properties;
	private Map<QName,List<NodeRef>> targetAssocs;
	private Map<QName,List<NodeRef>> sourceAssocs;
	private Map<QName,List<NodeRef>> childAssocs;

	public NodeInfo() {
		properties = new HashMap<QName,Serializable>();
		targetAssocs = new HashMap<QName,List<NodeRef>>();
		childAssocs = new HashMap<QName,List<NodeRef>>();
	}
	
	public QName getType() {
		return type;
	}

	public void setType(QName type) {
		this.type = type;
	}

	public List<QName> getAspects() {
	    return aspects;
	}

	public void setAspects(List<QName> aspects) {
	    this.aspects = aspects;
	}
	
	public void setAspects(Set<QName> aspects) {
	    this.aspects = new ArrayList<>(aspects);
	}

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public NodeRef getParent() {
		return parent;
	}

	public void setParent(NodeRef parent) {
		this.parent = parent;
	}
	
	public QName getParentAssoc() {
		return parentAssoc;
	}

	public void setParentAssoc(QName parentAssoc) {
		this.parentAssoc = parentAssoc;
	}
	
	public Map<QName,Serializable> getProperties() {
		return properties;
	}
	
	public Serializable getProperty(QName propertyName) {
		return properties.get(propertyName);
	}

	public void setProperties(Map<QName,Serializable> properties) {
		this.properties = properties;
	}
	
	public void setProperty(QName propertyName, Serializable value) {
		if(properties == null) {
			properties = new HashMap<QName,Serializable>();
		}
		properties.put(propertyName, value);
	}
	
	public Map<QName,List<NodeRef>> getTargetAssocs() {
		return targetAssocs;
	}
	
	public void setTargetAssocs(Map<QName,List<NodeRef>> targetAssocs) {
		this.targetAssocs = targetAssocs;
	}
	
    public void setTargetAssocs(List<NodeRef> targetRefs, QName associationName) {
        targetAssocs = setAssociations(targetAssocs, targetRefs, associationName);
    }
    
    public void createTargetAssociation(NodeRef targetRef, QName associationName) {
        targetAssocs = createAssociations(targetAssocs, Collections.singletonList(targetRef), associationName);
    }
    
    public void createTargetAssociations(List<NodeRef> targetRefs, QName associationName) {
        targetAssocs = createAssociations(targetAssocs, targetRefs, associationName);
    }
    
    public void removeTargetAssociation(NodeRef targetRef, QName associationName) {
        targetAssocs = removeAssociations(targetAssocs, Collections.singletonList(targetRef), associationName);
    }

    public void removeTargetAssociations(List<NodeRef> targetRefs, QName associationName) {
        targetAssocs = removeAssociations(targetAssocs, targetRefs, associationName);
    }

    public Map<QName, List<NodeRef>> getSourceAssocs() {
        return sourceAssocs;
    }

    public void setSourceAssocs(Map<QName, List<NodeRef>> sourceAssocs) {
        this.sourceAssocs = sourceAssocs;
    }

    public void setSourceAssocs(List<NodeRef> sourceRefs, QName associationName) {
        sourceAssocs = setAssociations(sourceAssocs, sourceRefs, associationName);
    }
    
    public void createSourceAssociation(NodeRef sourceRef, QName assosiationName) {
        sourceAssocs = createAssociations(sourceAssocs, Collections.singletonList(sourceRef), assosiationName);
    }

    public void createSourceAssociations(List<NodeRef> sourceRefs, QName assosiationName) {
        sourceAssocs = createAssociations(sourceAssocs, sourceRefs, assosiationName);
    }

    public void removeSourceAssociation(NodeRef sourceRef, QName associationName) {
        sourceAssocs = removeAssociations(sourceAssocs, Collections.singletonList(sourceRef), associationName);
    }

    public void removeSourceAssociations(List<NodeRef> sourceRefs, QName associationName) {
        sourceAssocs = removeAssociations(sourceAssocs, sourceRefs, associationName);
    }

    public Map<QName,List<NodeRef>> getChildAssocs() {
		return childAssocs;
	}
	
	public void setChildAssocs(Map<QName,List<NodeRef>> childAssocs) {
		this.childAssocs = childAssocs;
	}
	
	public void setChildAssocs(List<NodeRef> childRefs, QName associationName) {
	    childAssocs = setAssociations(childAssocs, childRefs, associationName);
	}
	
	public void addChild(NodeRef childRef, QName associationName) {
		childAssocs = createAssociations(childAssocs, Collections.singletonList(childRef), associationName);
	}
	
	public void removeChild(NodeRef childRef, QName associationName) {
		childAssocs = removeAssociations(childAssocs, Collections.singletonList(childRef), associationName);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(nodeRef != null) {
			sb.append("(Node: ");
			sb.append(nodeRef.toString());
			sb.append(")");
		}
		if(type != null) {
			sb.append("(Type: ");
			sb.append(type.toString());
			sb.append(")");
		}
		if(parent != null) {
			sb.append("(Parent: ");
			sb.append(parent.toString());
			sb.append(")");
		}
		if(properties != null) {
			sb.append(properties.toString());
		}
		if(targetAssocs != null) {
		    sb.append(targetAssocs);
		}
		return sb.toString();
	}
	
    @Override
    public boolean equals(Object that) {
        if(that instanceof NodeInfo) {
            return equals((NodeInfo)that);
        } else {
            return false;
        }
    }
    
    public boolean equals(NodeInfo that) {
        return ObjectUtils.equals(this.properties, that.properties);
    }
    
    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(properties);
    }
    
	/////////////////////////////////////////////////////////////////
	//                        PRIVATE STAFF                        //
	/////////////////////////////////////////////////////////////////
	
    private static Map<QName, List<NodeRef>> createAssociations(Map<QName, List<NodeRef>> assocs, List<NodeRef> targetRefs, QName associationName) {
		if(assocs == null) {
			assocs = new HashMap<QName,List<NodeRef>>();
		}
		List<NodeRef> nodes = assocs.get(associationName);
		if(nodes == null) {
			nodes = new ArrayList<NodeRef>();
			assocs.put(associationName, nodes);
		}
		for(NodeRef targetRef : targetRefs) {
    		if(!nodes.contains(targetRef)) {
    			nodes.add(targetRef);
    		}
		}
		return assocs;
	}

    private static Map<QName, List<NodeRef>> setAssociations(Map<QName, List<NodeRef>> assocs, List<NodeRef> targetRefs, QName associationName) {
        if(assocs == null) {
            assocs = new HashMap<QName,List<NodeRef>>();
        }
        assocs.put(associationName, new ArrayList<NodeRef>(targetRefs));
        return assocs;
    }

	private static Map<QName, List<NodeRef>> removeAssociations(Map<QName, List<NodeRef>> assocs, List<NodeRef> targetRefs, QName associationName) {
		if(assocs == null) {
			return assocs;
		}
		List<NodeRef> nodes = assocs.get(associationName);
		if(nodes == null) {
			return assocs;
		}
		for(NodeRef targetRef : targetRefs) {
    		if(nodes.contains(targetRef)) {
    			nodes.remove(targetRef);
    		}
		}
		return assocs;
	}

}
