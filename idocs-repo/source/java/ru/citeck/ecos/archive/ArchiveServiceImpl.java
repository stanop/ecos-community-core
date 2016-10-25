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
package ru.citeck.ecos.archive;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.archive.ArchiveServicePolicies.BeforeMovePolicy;
import ru.citeck.ecos.archive.ArchiveServicePolicies.OnMovePolicy;
import ru.citeck.ecos.model.ArchiveServiceConfigurationModel;

import java.io.Serializable;
import java.util.*;

/**
 * The main goal of this service is a moving documents into the archive.
 * 
 * It takes configuration of archived destination from nodes of 
 * arch:archConfig type. There is a global journal where you can
 * specify in an arch:archConfig: node type, association type, destination node
 * reference. Where node type is a type of archived nodes; association type is
 * an association type between archived node and destination node.
 * 
 * If you try to move into archive a node, which type is not specified in
 * configuration arch:archConfig, it tries to find configuration for
 * sub-types of node type.
 * 
 * There are mandatory fields: <code>policyComponent, nodeService,
 * tenantService, searchService, dictionaryService</code>.
 * 
 * @author Ruslan
 * 
 */
class ArchiveServiceImpl implements ArchiveService {

	private static final Log log = LogFactory.getLog(ArchiveServiceImpl.class);

	/**
	 * It is used for registering archive service policies.
	 */
	protected PolicyComponent policyComponent;
	/**
	 * It moves the specified node to the destination node.
	 */
	protected NodeService nodeService;
	protected TenantService tenantService;
	protected SearchService searchService;
	protected DictionaryService dictionaryService;
	protected Set<String> storesToIgnorePolicies = Collections.emptySet();

	protected Map<QName, Destination> cache = Collections.synchronizedMap(new HashMap<QName, Destination>());

	private ClassPolicyDelegate<BeforeMovePolicy> beforeMoveDelegate;
	private ClassPolicyDelegate<OnMovePolicy> onMoveDelegate;

	public void init() throws Exception {
		if (log.isInfoEnabled())
			log.info("Initializing ArchiveService...");

		if (policyComponent == null)
			throw new Exception("PolicyComponent is not specified");
		if (nodeService == null)
			throw new Exception("NodeService is not specified");

		beforeMoveDelegate = policyComponent
				.registerClassPolicy(ArchiveServicePolicies.BeforeMovePolicy.class);
		onMoveDelegate = policyComponent
				.registerClassPolicy(ArchiveServicePolicies.OnMovePolicy.class);
	}

	/**
	 * @see ArchiveService#move(NodeRef, String)
	 */
	@Override
	public void move(NodeRef nodeRef, String cause) throws InvalidNodeRefException,
			CyclicChildRelationshipException {

		if (log.isDebugEnabled())
			log.debug("ArchiveService: moving to the archive nodeRef=" + nodeRef);

		if (!nodeService.exists(nodeRef)) {
			if (log.isDebugEnabled())
				log.debug("ArchiveService: Can not move node, which is not exists. nodeRef=" + nodeRef);
			return;
		}

		QName nodeType = nodeService.getType(nodeRef);
		Destination d = getDestination(nodeRef, nodeType);
		if (d == null) {
			throw new AlfrescoRuntimeException("Can not move node to archive, because there is no destination for node type. nodeRef=" + nodeRef + ", type=" + nodeType);
		}
		else {
			invokeBeforeMoveNode(nodeRef, d.getDestination(), cause);
			ChildAssociationRef newChildAssocRef = nodeService.moveNode(
					nodeRef,
					d.getDestination(),
					d.getAssocType(),
					nodeService.getPrimaryParent(nodeRef).getQName());
			invokeOnMoveNode(nodeRef, newChildAssocRef, cause);
			if (log.isDebugEnabled())
				log.debug("ArchiveService: The node successufully moved to the archive. nodeRef=" + nodeRef);
		}
	}

	@Override
	public void clearCache() {
		cache.clear();
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	public void setStoresToIgnorePolicies(Set<String> storesToIgnorePolicies) {
		this.storesToIgnorePolicies = storesToIgnorePolicies;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * @see ArchiveServicePolicies.BeforeMovePolicy#beforeMoveDocument(targetNodeRef,
	 *      newParentRef)
	 * @param targetNodeRef
	 *            - a node reference of the document
	 * @param newParentRef
	 *            - a destination folder node reference
	 * @param cause
	 *            - internal parameter, used in behavior
	 */
	protected void invokeBeforeMoveNode(
			NodeRef targetNodeRef,
			NodeRef newParentRef,
			String cause) {
		if (ignorePolicy(targetNodeRef)) {
			return;
		}
		// get qnames to invoke against
		Set<QName> qnames = getTypeAndAspectQNames(targetNodeRef);
		// execute policy for node type and aspects
		ArchiveServicePolicies.BeforeMovePolicy policy = beforeMoveDelegate
				.get(targetNodeRef, qnames);
		policy.beforeMoveDocument(targetNodeRef, newParentRef, cause);
	}

	/**
	 * @see ArchiveServicePolicies.OnMovePolicy#onMoveDocument(NodeRef,
	 *      ChildAssociationRef)
	 * @param targetNodeRef
	 *            - a node reference of the document
	 * @param newChildAssocRef
	 *            - a destination folder child association reference
	 * @param cause
	 *            - internal parameter, used in behavior
	 */
	protected void invokeOnMoveNode(
			NodeRef targetNodeRef,
			ChildAssociationRef newChildAssocRef,
			String cause) {
		if (ignorePolicy(targetNodeRef)) {
			return;
		}
		// get qnames to invoke against
		Set<QName> qnames = getTypeAndAspectQNames(targetNodeRef);
		// execute policy for node type and aspects
		ArchiveServicePolicies.OnMovePolicy policy = onMoveDelegate.get(
				targetNodeRef, qnames);
		policy.onMoveDocument(targetNodeRef, newChildAssocRef, cause);
	}

	/**
	 * Get all aspect and node type qualified names
	 * 
	 * @param nodeRef
	 *            the node we are interested in
	 * @return Returns a set of qualified names containing the node type and all
	 *         the node aspects, or null if the node no longer exists
	 */
	protected Set<QName> getTypeAndAspectQNames(NodeRef nodeRef) {
		Set<QName> qnames = null;
		try {
			Set<QName> aspectQNames = nodeService.getAspects(nodeRef);
			QName typeQName = nodeService.getType(nodeRef);

			qnames = new HashSet<QName>(aspectQNames.size() + 1);
			qnames.addAll(aspectQNames);
			qnames.add(typeQName);
		} catch (InvalidNodeRefException e) {
			qnames = Collections.emptySet();
		}
		return qnames;
	}

	private boolean ignorePolicy(NodeRef nodeRef) {
		if (storesToIgnorePolicies == null || tenantService == null)
			return false;

		return (storesToIgnorePolicies.contains(tenantService.getBaseName(
				nodeRef.getStoreRef()).toString()));
	}

	private Destination getDestination(NodeRef nodeRef, QName nodeType) {
		Destination result = cache.get(nodeType);
		if (result == null) {
			ChildAssociationRef parentRef = nodeService.getPrimaryParent(nodeRef);
			QName defaultAssocType = parentRef.getTypeQName();

			result = getDestinationInternal(nodeType, defaultAssocType);
			if (result == null) {
				TypeDefinition typeDefinition = dictionaryService.getType(nodeType);
				ClassDefinition classDefinition = typeDefinition.getParentClassDefinition();
				while (classDefinition != null) {
					result = getDestinationInternal(classDefinition.getName(), defaultAssocType);
					if (result != null)
						break;
					classDefinition = classDefinition.getParentClassDefinition();
				}
			}
			if (result != null)
				cache.put(nodeType, result);
		}
		else {
			if (log.isDebugEnabled())
				log.debug("Destination has got from cache. nodeRef=" + nodeRef + ", nodeType=" + nodeType);
		}
		return result;
	}

	protected Destination getDestinationInternal(QName nodeType, QName defaultAssocType) {
		Destination result = null;
		// For long qname search is working...
		String query = "TYPE:\"" + ArchiveServiceConfigurationModel.TYPE_ARCH_CONFIG + "\" AND @arch\\:nodeType:\"" + nodeType + "\"";
		if (log.isDebugEnabled())
			log.debug("   Search query: " + query);
		ResultSet rs = null;
		try {
			rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, query);
			if (rs.length() > 0) {
				if (log.isDebugEnabled())
					log.debug("      Query result contains " + rs.length() + " records.");
				QName assocType = defaultAssocType;
				NodeRef destination = null;
				ResultSetRow row = rs.getRow(0);
				Serializable assocTypeObj = row.getValue(ArchiveServiceConfigurationModel.PROP_ASSOC_TYPE);
				Serializable destinationObj = row.getValue(ArchiveServiceConfigurationModel.PROP_DESTINATION);
				if (assocTypeObj instanceof QName)
					assocType = (QName)assocTypeObj;
				if (destinationObj instanceof NodeRef)
					destination = (NodeRef)destinationObj;
				if (destination != null) {
					result = new Destination(destination, assocType);
					if (log.isDebugEnabled())
						log.debug("      destination=" + result);
				}
			}
		}
		finally {
			if (rs != null)
				rs.close();
		}
		return result;
	}

	private final class Destination {
		private final NodeRef destination;
		private final QName assocType;

		public Destination(NodeRef destination, QName assocType) {
			super();
			this.destination = destination;
			this.assocType = assocType;
		}

		public NodeRef getDestination() {
			return destination;
		}

		public QName getAssocType() {
			return assocType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((assocType == null) ? 0 : assocType.hashCode());
			result = prime * result
					+ ((destination == null) ? 0 : destination.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Destination other = (Destination) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (assocType == null) {
				if (other.assocType != null)
					return false;
			} else if (!assocType.equals(other.assocType))
				return false;
			if (destination == null) {
				if (other.destination != null)
					return false;
			} else if (!destination.equals(other.destination))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Destination [destination=" + destination + ", assocType="
					+ assocType + "]";
		}

		private ArchiveServiceImpl getOuterType() {
			return ArchiveServiceImpl.this;
		}

	}

}
