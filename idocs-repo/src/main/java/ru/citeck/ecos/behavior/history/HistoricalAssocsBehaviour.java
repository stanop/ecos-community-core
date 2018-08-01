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
package ru.citeck.ecos.behavior.history;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.behavior.JavaBehaviour;
import ru.citeck.ecos.history.HistoryService;
import ru.citeck.ecos.history.HistoryUtils;
import ru.citeck.ecos.model.HistoryModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoricalAssocsBehaviour implements
		NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnDeleteAssociationPolicy
{

	private PolicyComponent policyComponent;
	private NodeService nodeService;
	private HistoryService historyService;

	private List<QName> allowedAssocs;
	private DictionaryService dictionaryService;
	private static Map<String,Long> createdNodes = new HashMap<String,Long>();
	protected boolean enableHistoryOnAddAssocs;
	protected boolean enableHistoryOnDeleteAssocs;

	private static final Log logger = LogFactory.getLog(HistoricalAssocsBehaviour.class);

	public void init() {

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT)
		);

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onDeleteAssociation", NotificationFrequency.TRANSACTION_COMMIT)
		);
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setHistoryService(HistoryService historyService) {
		this.historyService = historyService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	private boolean isNewNode(NodeRef nodeRef) {
		NodeRef.Status status = nodeService.getNodeStatus(nodeRef);
		synchronized (createdNodes) {
			Long createdDbTxnId = createdNodes.get(nodeRef.getId());
			if (createdDbTxnId!=null) {
				if (createdDbTxnId.equals(status.getDbTxnId())) {
					return true;
				} else {
					// Remove from cache if not new
					createdNodes.remove(nodeRef.getId());
					return false;
				}
			} else {
				return false;
			}
		}
	}

	@Override
	public void onCreateAssociation(AssociationRef nodeAssocRef) {
		if (!allowedAssocs.contains(nodeAssocRef.getTypeQName())) {
			return;
		}

        AssociationDefinition assoc = dictionaryService.getAssociation(nodeAssocRef.getTypeQName());
        NodeRef nodeSource = nodeAssocRef.getSourceRef();
        NodeRef nodeTarget = nodeAssocRef.getTargetRef();
        if (!isNewNode(nodeAssocRef.getSourceRef())
				&& enableHistoryOnAddAssocs
				&& nodeService.exists(nodeSource)
				&& nodeService.exists(nodeTarget)
				&& allowedAssocs != null) {
            if (assoc != null) {
				historyService.persistEvent(
						HistoryModel.TYPE_BASIC_EVENT,
						HistoryUtils.eventProperties(
								HistoryUtils.ASSOC_UPDATED,
								nodeSource,
								assoc.getName(),
								nodeTarget.toString(),
								HistoryUtils.getAssocCommentForSourceAndTarget(nodeAssocRef,
										null,
										true,
										dictionaryService,
										nodeService),
								null,
								null
						));
				historyService.persistEvent(
						HistoryModel.TYPE_BASIC_EVENT,
						HistoryUtils.eventProperties(
								HistoryUtils.ASSOC_UPDATED,
								nodeTarget,
								assoc.getName(),
								nodeSource.toString(),
								HistoryUtils.getAssocCommentForSourceAndTarget(nodeAssocRef,
										null,
										false,
										dictionaryService,
										nodeService),
								null,
								null
						));
			}
		}
	}

	@Override
	public void onDeleteAssociation(AssociationRef nodeAssocRef) {
		if (!allowedAssocs.contains(nodeAssocRef.getTypeQName())) {
			return;
		}

		NodeRef nodeSource = nodeAssocRef.getSourceRef();
		NodeRef nodeTarget = nodeAssocRef.getTargetRef();
		AssociationDefinition assoc = dictionaryService.getAssociation(nodeAssocRef.getTypeQName());
		if (!isNewNode(nodeAssocRef.getSourceRef())
				&& enableHistoryOnDeleteAssocs
				&& nodeService.exists(nodeSource)
				&& nodeService.exists(nodeTarget)
				&& allowedAssocs != null) {
			if (assoc != null) {
				historyService.persistEvent(
						HistoryModel.TYPE_BASIC_EVENT,
						HistoryUtils.eventProperties(
								HistoryUtils.ASSOC_UPDATED,
								nodeSource,
								assoc.getName(),
								nodeTarget.toString(),
								HistoryUtils.getAssocCommentForSourceAndTarget(null,
										nodeAssocRef,
										true,
										dictionaryService,
										nodeService),
								null,
								null
						));
				historyService.persistEvent(
						HistoryModel.TYPE_BASIC_EVENT,
						HistoryUtils.eventProperties(
								HistoryUtils.ASSOC_UPDATED,
								nodeTarget,
								assoc.getName(),
								nodeSource.toString(),
								HistoryUtils.getAssocCommentForSourceAndTarget(null,
										nodeAssocRef,
										false,
										dictionaryService,
										nodeService),
								null,
								null
						));
			}
		}
	}

	public void setAllowedAssocs(List<QName> allowedAssocs) {
		this.allowedAssocs = allowedAssocs;
	}

	/**
	 * enabled
	 * @param true or false
	 */
	public void setEnableHistoryOnAddAssocs(Boolean enableHistoryOnAddAssocs) {
		this.enableHistoryOnAddAssocs = enableHistoryOnAddAssocs.booleanValue();
	}

	/**
	 * enabled
	 * @param true or false
	 */
	public void setEnableHistoryOnDeleteAssocs(Boolean enableHistoryOnDeleteAssocs) {
		this.enableHistoryOnDeleteAssocs = enableHistoryOnDeleteAssocs.booleanValue();
	}
}
