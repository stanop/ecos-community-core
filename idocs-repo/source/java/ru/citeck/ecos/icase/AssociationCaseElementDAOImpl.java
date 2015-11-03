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
package ru.citeck.ecos.icase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

import ru.citeck.ecos.behavior.ParameterizedJavaBehaviour;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.utils.RepoUtils;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class AssociationCaseElementDAOImpl extends AbstractCaseElementDAO {

    @Override
    public QName getElementConfigType() {
        return ICaseModel.TYPE_ASSOC_CONFIG;
    }
    
    enum AssociationType {
        TARGET {
            @Override
            public List<NodeRef> getTarget(NodeRef caseNode, QName assocName,
                    NodeService nodeService) {
                return searchTargetAssocs(caseNode, assocName, nodeService);
            }

            @Override
            public List<NodeRef> getSource(NodeRef element, QName assocName,
                    NodeService nodeService) {
                return searchSourceAssocs(element, assocName, nodeService);
            }

            @Override
            public void create(NodeRef caseNode, NodeRef element,
                    QName assocName, NodeService nodeService) {
                nodeService.createAssociation(caseNode, element, assocName);
            }

            @Override
            public void remove(NodeRef caseNode, NodeRef element,
                    QName assocName, NodeService nodeService) {
                nodeService.removeAssociation(caseNode, element, assocName);
            }

            @Override
            public NodeRef getCaseNode(AssociationRef assocRef) {
                return assocRef.getSourceRef();
            }

            @Override
            public NodeRef getElementNode(AssociationRef assocRef) {
                return assocRef.getTargetRef();
            }
        },

        SOURCE {
            @Override
            public List<NodeRef> getTarget(NodeRef caseNode, QName assocName,
                    NodeService nodeService) {
                return searchSourceAssocs(caseNode, assocName, nodeService);
            }

            @Override
            public List<NodeRef> getSource(NodeRef element, QName assocName,
                    NodeService nodeService) {
                return searchTargetAssocs(element, assocName, nodeService);
            }

            @Override
            public void create(NodeRef caseNode, NodeRef element,
                    QName assocName, NodeService nodeService) {
                nodeService.createAssociation(element, caseNode, assocName);
            }

            @Override
            public void remove(NodeRef caseNode, NodeRef element,
                    QName assocName, NodeService nodeService) {
                nodeService.removeAssociation(element, caseNode, assocName);
            }

            @Override
            public NodeRef getCaseNode(AssociationRef assocRef) {
                return assocRef.getTargetRef();
            }

            @Override
            public NodeRef getElementNode(AssociationRef assocRef) {
                return assocRef.getSourceRef();
            }
            
        },

        CHILD {
            @Override
            public List<NodeRef> getTarget(NodeRef caseNode, QName assocName,
                    NodeService nodeService) {
                return searchChildAssocs(caseNode, assocName, nodeService);
            }

            @Override
            public List<NodeRef> getSource(NodeRef element, QName assocName,
                    NodeService nodeService) {
                return searchParentAssocs(element, assocName, nodeService);
            }

            @Override
            public void create(NodeRef caseNode, NodeRef element,
                    QName assocName, NodeService nodeService) {
//                nodeService.addChild(caseNode, element, assocName, nodeService.getPrimaryParent(element).getQName());
                nodeService.moveNode(element, caseNode, assocName, nodeService.getPrimaryParent(element).getQName());
            }

            @Override
            public void remove(NodeRef caseNode, NodeRef element,
                    QName assocName, NodeService nodeService) {
                // first check if the association is primary
                ChildAssociationRef primaryAssoc = nodeService.getPrimaryParent(element);
                if(matches(primaryAssoc, caseNode, assocName)) {
                    nodeService.deleteNode(element);
                    return;
                }
                
                // otherwise remove all matching parent assocs
                // (presumably, there are less parent assocs of element, than child assocs of case)
                List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(element, assocName, RegexQNamePattern.MATCH_ALL);
                for(ChildAssociationRef parentAssoc : parentAssocs) {
                    if(matches(parentAssoc, caseNode, assocName)) {
                        nodeService.removeChildAssociation(parentAssoc);
                    }
                }
            }

            private boolean matches(ChildAssociationRef assoc, NodeRef requiredParent, QName requiredType) {
                return assoc.getParentRef().equals(requiredParent) && assoc.getTypeQName().equals(requiredType);
            }
            
            public NodeRef getCaseNode(ChildAssociationRef assocRef) {
                return assocRef.getParentRef();
            }
            
            public NodeRef getElementNode(ChildAssociationRef assocRef) {
                return assocRef.getChildRef();
            }
            
        };

        public static AssociationType forName(String name) {
            for (AssociationType type : values()) {
                if (type.toString().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }

        public abstract List<NodeRef> getTarget(NodeRef caseNode,
                QName assocName, NodeService nodeService);

        public abstract List<NodeRef> getSource(NodeRef element,
                QName assocName, NodeService nodeService);

        public abstract void create(NodeRef caseNode, NodeRef element,
                QName assocName, NodeService nodeService);

        public abstract void remove(NodeRef caseNode, NodeRef element,
                QName assocName, NodeService nodeService);
        
        public NodeRef getCaseNode(AssociationRef assocRef) {
            throw new IllegalStateException();
        }
        
        public NodeRef getElementNode(AssociationRef assocRef) {
            throw new IllegalStateException();
        }
        
        public NodeRef getCaseNode(ChildAssociationRef assocRef) {
            throw new IllegalStateException();
        }
        
        public NodeRef getElementNode(ChildAssociationRef assocRef) {
            throw new IllegalStateException();
        }
        
        
    }
    
    private AssociationType needAssociationType(NodeRef config) {
        String associationType = RepoUtils.getMandatoryProperty(config, ICaseModel.PROP_ASSOC_TYPE, nodeService);
        return AssociationType.valueOf(associationType.toUpperCase());
    }

    public QName needAssocName(NodeRef config) {
        QName assocName = RepoUtils.getMandatoryProperty(config, ICaseModel.PROP_ASSOC_NAME, nodeService);
        return assocName;
    }

    @Override
	public List<NodeRef> get(NodeRef caseNode, NodeRef config)
			throws AlfrescoRuntimeException, IllegalArgumentException {
	
		if (!nodeService.exists(caseNode)) {
			throw new IllegalArgumentException("Cannot get nodeRefs without case node");
		}
		if (!nodeService.exists(config)) {
			throw new IllegalArgumentException("Cannot get nodeRefs without config");
		}
		AssociationType associationType = needAssociationType(config);
		QName assocName = needAssocName(config);
		try {
		    List<NodeRef> elements = associationType.getTarget(caseNode, assocName, nodeService);
			QName elementType = needElementType(config);
			return filterByClass(elements, elementType);
		}
		catch(IllegalArgumentException e) {
			throw e;
		}
		catch(Throwable e) {
			throw new AlfrescoRuntimeException("Can not get elements from case. caseNode=" + caseNode + "; config=" + config, e);
		}
	}

    @Override
    protected List<NodeRef> getCasesImpl(NodeRef element, NodeRef config)
            throws AlfrescoRuntimeException, IllegalArgumentException {
        AssociationType associationType = needAssociationType(config);
        QName assocName = needAssocName(config);
        try {
            return associationType.getSource(element, assocName, nodeService);
        }
        catch(IllegalArgumentException e) {
            throw e;
        }
        catch(Throwable e) {
            throw new AlfrescoRuntimeException("Can not get cases for element. element=" + element + "; config=" + config, e);
        }
    }
    
	@Override
	public void add(NodeRef nodeRef, NodeRef caseNode, NodeRef config)
			throws AlfrescoRuntimeException, IllegalArgumentException {

		if (!nodeService.exists(caseNode)) {
			throw new IllegalArgumentException("Cannot create the association without caseNode");
		}
		if (!nodeService.exists(nodeRef)) {
			throw new IllegalArgumentException("Cannot create the association without nodeRef");
		}
		if (!nodeService.exists(config)) {
			throw new IllegalArgumentException("Cannot create the association without config");
		}
		AssociationType associationType = needAssociationType(config);
		QName assocName = needAssocName(config);
		try {
			associationType.create(caseNode, nodeRef, assocName, nodeService);
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		catch (Throwable e) {
			throw new AlfrescoRuntimeException("Can not add node reference to case. nodeRef=" + nodeRef + "; caseNode=" + caseNode + "; config=" + config, e);
		}
	}

	@Override
	public void remove(NodeRef nodeRef, NodeRef caseNode, NodeRef config)
			throws AlfrescoRuntimeException, IllegalArgumentException {

		if (!nodeService.exists(caseNode)) {
			throw new IllegalArgumentException("Cannot remove the association without caseNode");
		}
		if (!nodeService.exists(nodeRef)) {
			throw new IllegalArgumentException("Cannot remove the association without nodeRef");
		}
		if (!nodeService.exists(config)) {
			throw new IllegalArgumentException("Cannot remove the association without config");
		}
		AssociationType associationType = needAssociationType(config);
		QName assocName = needAssocName(config);
		try {
			associationType.remove(caseNode, nodeRef, assocName, nodeService);
		}
		catch(IllegalArgumentException e) {
			throw e;
		}
		catch(Throwable e) {
			throw new AlfrescoRuntimeException("Can not add node reference to case. nodeRef=" + nodeRef + "; caseNode=" + caseNode + "; config=" + config, e);
		}
	}

    private static List<NodeRef> searchSourceAssocs(NodeRef nodeRef, QName association, NodeService nodeService) {
        List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(nodeRef, association);
        ArrayList<NodeRef> caseObjects = new ArrayList<NodeRef>();
        for (AssociationRef sourceAssoc : sourceAssocs) {
            caseObjects.add(sourceAssoc.getSourceRef());
        }
        return caseObjects;
    }

    private static List<NodeRef> searchTargetAssocs(NodeRef nodeRef, QName association, NodeService nodeService) {
        List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(nodeRef, association);
        ArrayList<NodeRef> caseObjects = new ArrayList<NodeRef>();
        for (AssociationRef targetAssoc : targetAssocs) {
            caseObjects.add(targetAssoc.getTargetRef());
        }
        return caseObjects;
    }

    private static List<NodeRef> searchChildAssocs(NodeRef nodeRef, QName association, NodeService nodeService) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef, association, RegexQNamePattern.MATCH_ALL);
        ArrayList<NodeRef> caseObjects = new ArrayList<NodeRef>(childAssocs.size());
        for (ChildAssociationRef childAssoc : childAssocs) {
            caseObjects.add(childAssoc.getChildRef());
        }
        return caseObjects;
    }

    private static List<NodeRef> searchParentAssocs(NodeRef nodeRef, QName association, NodeService nodeService) {
        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef, association, RegexQNamePattern.MATCH_ALL);
        ArrayList<NodeRef> caseObjects = new ArrayList<NodeRef>(parentAssocs.size());
        for (ChildAssociationRef parentAssoc : parentAssocs) {
            caseObjects.add(parentAssoc.getParentRef());
        }
        return caseObjects;
    }

    @Override
    public Set<BehaviourDefinition<?>> intializeBehaviours(NodeRef config) {
        Set<BehaviourDefinition<?>> behaviours = new HashSet<>(2);
        AssociationType associationType = needAssociationType(config);
        QName assocName = needAssocName(config);
        switch(associationType) {
        case CHILD:
            behaviours.add(policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, 
                    ICaseModel.ASPECT_CASE, assocName, ParameterizedJavaBehaviour.newInstance(this, "onCaseElementAdd", config)));
            behaviours.add(policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteChildAssociationPolicy.QNAME, 
                    ICaseModel.ASPECT_CASE, assocName, ParameterizedJavaBehaviour.newInstance(this, "onCaseElementRemove", config)));
            break;
        case TARGET:
            behaviours.add(policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, 
                    ICaseModel.ASPECT_CASE, assocName, ParameterizedJavaBehaviour.newInstance(this, "onCaseElementAdd", config)));
            behaviours.add(policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, 
                    ICaseModel.ASPECT_CASE, assocName, ParameterizedJavaBehaviour.newInstance(this, "onCaseElementRemove", config)));
            break;
        case SOURCE:
            QName elementType = needElementType(config);
            behaviours.add(policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, 
                    elementType, assocName, ParameterizedJavaBehaviour.newInstance(this, "onCaseElementAdd", config)));
            behaviours.add(policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, 
                    elementType, assocName, ParameterizedJavaBehaviour.newInstance(this, "onCaseElementRemove", config)));
            break;
        }
        return behaviours;
    }
    
    private class BehaviourEnvironment {
        AssociationType associationType;
        QName requiredType;
        NodeRef caseRef, element, config;
        
        public BehaviourEnvironment(AssociationRef assocRef, NodeRef config) {
            this.config = config;
            if(!nodeService.exists(config)) return;
            
            associationType = needAssociationType(config);
            requiredType = needElementType(config);
            caseRef = associationType.getCaseNode(assocRef);
            element = associationType.getElementNode(assocRef);
        }
        
        public BehaviourEnvironment(ChildAssociationRef assocRef, NodeRef config) {
            this.config = config;
            if(!nodeService.exists(config)) return;
            
            associationType = needAssociationType(config);
            if(associationType != AssociationType.CHILD) return;
            
            requiredType = needElementType(config);
            caseRef = associationType.getCaseNode(assocRef);
            element = associationType.getElementNode(assocRef);
            
        }
        
        public boolean isValid() {
            return config != null && nodeService.exists(config)
                && caseRef != null && nodeService.exists(caseRef)
                && element != null && nodeService.exists(element)
                && nodeService.hasAspect(caseRef, ICaseModel.ASPECT_CASE)
                && dictionaryService.isSubClass(nodeService.getType(element), requiredType);
        }
        
        public void invokeCaseElementAddEvent() {
            if(!isValid()) return;
            caseElementService.invokeOnCaseElementAdd(caseRef, element, config);
        }

        public void invokeCaseElementRemoveEvent() {
            if(!isValid()) return;
            caseElementService.invokeOnCaseElementRemove(caseRef, element, config);
        }
        
    }
    
    public void onCaseElementAdd(AssociationRef assocRef, NodeRef config) {
        BehaviourEnvironment env = new BehaviourEnvironment(assocRef, config);
        env.invokeCaseElementAddEvent();
    }
    
    public void onCaseElementRemove(AssociationRef assocRef, NodeRef config) {
        BehaviourEnvironment env = new BehaviourEnvironment(assocRef, config);
        env.invokeCaseElementRemoveEvent();
    }
    
    public void onCaseElementAdd(ChildAssociationRef assocRef, boolean isNewNode, NodeRef config) {
        BehaviourEnvironment env = new BehaviourEnvironment(assocRef, config);
        env.invokeCaseElementAddEvent();
    }
    
    public void onCaseElementRemove(ChildAssociationRef assocRef, NodeRef config) {
        BehaviourEnvironment env = new BehaviourEnvironment(assocRef, config);
        env.invokeCaseElementRemoveEvent();
    }
    
}
