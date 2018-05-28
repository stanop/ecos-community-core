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
package ru.citeck.ecos.behavior.common;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.DmsModel;
import ru.citeck.ecos.template.GenerateContentActionExecuter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateFromTemplateBehaviour implements NodeServicePolicies.OnCreateNodePolicy {
    private static Log logger = LogFactory.getLog(CreateFromTemplateBehaviour.class);

    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private ServiceRegistry serviceRegistry;
    private QName className;
    private Boolean enabled = null;
    private int order = 500;

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, className,
            new OrderedBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT, order));
    }


    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        if (logger.isDebugEnabled()) {
            logger.debug("Call CreateFromTemplateBehaviour");
        }
        if (!enabled) {
            return;
        }
        NodeRef parent = childAssocRef.getParentRef();
        NodeRef node = childAssocRef.getChildRef();
        // check that nodes exist
        if (!nodeService.exists(parent) || !nodeService.exists(node)) {
            return;
        }

        Set<NodeRef> tags = new HashSet<NodeRef>();

        List<AssociationRef> associations = nodeService.getTargetAssocs(childAssocRef.getChildRef(), RegexQNamePattern.MATCH_ALL);
        for (AssociationRef association : associations) {
            NodeRef assocNodeRef = association.getTargetRef();
            fillWithTags(assocNodeRef, tags);
        }

        Map<QName, Serializable> properties = nodeService.getProperties(node);
        Set<QName> keys = properties.keySet();
        for (QName key : keys) {
            Serializable value = properties.get(key);
            if (value != null) {
                if (value instanceof NodeRef) {
                    NodeRef nodeRef = (NodeRef) value;
                    fillWithTags(nodeRef, tags);
                } else if (value instanceof List) {
                    fillWithTags((List<?>) value, tags);
                }
            }
        }
        for (NodeRef tag : tags) {
            logger.info(tag);
        }

        NodeRef template;
        if (nodeService.getTargetAssocs(node, DmsModel.ASSOC_TEMPLATE).size() > 0) {
            template = nodeService.getTargetAssocs(node, DmsModel.ASSOC_TEMPLATE).get(0).getTargetRef();
            nodeService.setProperty(node, DmsModel.PROP_UPDATE_CONTENT, true);
        } else {
            template = getTemplateBasedOnKind(node)!= null ? getTemplateBasedOnKind(node) : getTemplateBasedOnType(node);
        }

		if(template == null) {
            template = getTemplateBasedOnTag(node, tags);
		}
		if(template != null) {
			if (!containsAssoc(node, template)) {
                nodeService.createAssociation(node, template, DmsModel.ASSOC_TEMPLATE);
            }
            /*added generate template*/
            boolean updateContent = (boolean) nodeService.getProperty(node, DmsModel.PROP_UPDATE_CONTENT);
            if (updateContent) {
                ActionService actionService = serviceRegistry.getActionService();
                Action actionGenerateContent = actionService.createAction(GenerateContentActionExecuter.NAME);
                actionService.executeAction(actionGenerateContent, node);
            }
            /*end*/
		}
        else {
            logger.info("Can't find template for document");
        }
    }

    private boolean containsAssoc(NodeRef node, NodeRef template) {
        List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(node, DmsModel.ASSOC_TEMPLATE);
        for (AssociationRef targetAssoc : targetAssocs) {
            if (targetAssoc.getTargetRef().equals(template)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void fillWithTags(NodeRef nodeRef, Set<NodeRef> tags) {
        Serializable nodeTags = nodeService.getProperty(nodeRef, ContentModel.PROP_TAGS);
        if (nodeTags != null) {
            if (nodeTags instanceof NodeRef) {
                tags.add((NodeRef) nodeTags);
            } else if (nodeTags instanceof List) {
                tags.addAll((List<NodeRef>) nodeTags);
            }
        }
    }

    private <T> void fillWithTags(List<T> nodes, Set<NodeRef> tags) {
        for (T nodeItem : nodes) {
            if (nodeItem instanceof NodeRef) {
                fillWithTags((NodeRef) nodeItem, tags);
            }
            if (nodeItem instanceof String && NodeRef.isNodeRef((String) nodeItem)) {
                NodeRef nodeRef = new NodeRef((String) nodeItem);
                if (nodeService.exists(nodeRef)) {
                    fillWithTags(nodeRef, tags);
                }
            }
        }
    }
	
	private NodeRef getTemplateBasedOnType(NodeRef node)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("TYPE:\"").append(DmsModel.TYPE_TEMPLATE).append("\" AND (");
		sb.append("@tk\\:appliesToType:\"")
                .append((NodeRef)nodeService.getProperty(node, ClassificationModel.PROP_DOCUMENT_TYPE))
                .append("\" AND (ISNULL:\"" + ClassificationModel.PROP_DOCUMENT_APPLIES_TO_KIND + "\"")
                .append("OR ISUNSET:\"" + ClassificationModel.PROP_DOCUMENT_APPLIES_TO_KIND + "\")")
                .append(")");
		NodeRef template = getTemplate(sb);
		return template;
	}
	
	private NodeRef getTemplateBasedOnKind(NodeRef node)
	{
		NodeRef template = null;
		if(nodeService.getProperty(node, ClassificationModel.PROP_DOCUMENT_KIND)!=null)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("TYPE:\"").append(DmsModel.TYPE_TEMPLATE).append("\"");
			sb.append(" AND (@tk\\:appliesToKind:\"").append((NodeRef)nodeService.getProperty(node, ClassificationModel.PROP_DOCUMENT_KIND)).append("\" )");
			template =  getTemplate(sb);
		}
		return template;
	}
	
	private NodeRef getTemplateBasedOnTag(NodeRef node, Set<NodeRef> tags)
	{
		NodeRef template = null;
        if (!tags.isEmpty()) {
			SearchParameters sp = new SearchParameters();
			StringBuilder sb = new StringBuilder();
			sb.append("TYPE:\"").append(DmsModel.TYPE_TEMPLATE).append("\" AND (");
			for (NodeRef tag : tags) {
				sb.append("@cm\\:taggable:\"").append(tag).append("\" ");
			}
			sb.append(")");
			template = getTemplate(sb);
		}
		return template;
	}
	
	private NodeRef getTemplate(StringBuilder sb)
	{
		ResultSet results = null;
		NodeRef template = null;
		SearchParameters sp = new SearchParameters();
		sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.setQuery(sb.toString());
		try {
			results = serviceRegistry.getSearchService().query(sp);
			List<NodeRef> templates = results.getNodeRefs();
			if (templates != null && !templates.isEmpty()) {
				template = templates.get(0);
			}
		} finally {
			if (results != null) {
				results.close();
			}
		}
		return template;
	}

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
