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
package ru.citeck.ecos.behavior;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.search.AssociationIndexPropertyRegistry;
import ru.citeck.ecos.utils.NodeUtils;
import ru.citeck.ecos.utils.TransactionUtils;

import java.io.Serializable;
import java.util.*;


public class AssociationIndexing implements OnCreateAssociationPolicy,
        OnDeleteAssociationPolicy {

    private static final String NODES_TO_UPDATE_TXN_KEY = AssociationIndexing.class.getName() + ".nodesToUpdate";
    private static final String ASSOCS_TO_UPDATE_TXN_KEY = AssociationIndexing.class.getName() + ".assocsToUpdate";

    private static Log logger = LogFactory.getLog(AssociationIndexing.class);

    private NodeService nodeService;
    private LockService lockService;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private AssociationIndexPropertyRegistry registry;
    private String typeQname;

    @Autowired
    private NodeUtils nodeUtils;

    public void init() {
        QName type = QName.createQName(typeQname);
        this.policyComponent.bindAssociationBehaviour(
                OnCreateAssociationPolicy.QNAME,
                type,
                new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT)
        );
        this.policyComponent.bindAssociationBehaviour(
                OnDeleteAssociationPolicy.QNAME,
                type,
                new JavaBehaviour(this, "onDeleteAssociation", NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onDeleteAssociation(AssociationRef nodeAssocRef) {
        NodeRef node = nodeAssocRef.getSourceRef();
        if (!nodeService.exists(node)) {
            return;
        }
        updateAssocMirrorProp(node, nodeAssocRef.getTypeQName());
    }

    @Override
    public void onCreateAssociation(AssociationRef nodeAssocRef) {
        NodeRef node = nodeAssocRef.getSourceRef();
        if (!nodeService.exists(node)) {
            return;
        }
        updateAssocMirrorProp(node, nodeAssocRef.getTypeQName());
    }

    public void updatePropertiesOnFullPersistedNodes(NodeRef node, QName assocQName, List<NodeRef> nodeRefs) {
        updateAssocMirrorProp(node, assocQName);
    }

    private void updateAssociationMirrorProperty(NodeRef node, QName assocQName) {
        updateAssocMirrorProp(node, assocQName);
        }

    private void updateAssocMirrorProp(NodeRef node, QName assocName) {
        Map<NodeRef, Set<QName>> assocsToUpdate = TransactionalResourceHelper.getMap(ASSOCS_TO_UPDATE_TXN_KEY);
        TransactionUtils.processBatchAfterCommit(NODES_TO_UPDATE_TXN_KEY, node, nodeRefs -> {
            for (NodeRef ref : nodeRefs) {
                if (nodeService.exists(ref)) {
                    updateMirrorProperties(ref, assocsToUpdate.get(ref));
                }
            }
        }, null);
        assocsToUpdate.computeIfAbsent(node, n -> new HashSet<>()).add(assocName);
    }

    private void updateMirrorProperties(NodeRef node, Set<QName> assocs) {
        try {
            behaviourFilter.disableBehaviour(node);

            LockStatus lockStatus = lockService.getLockStatus(node);
            switch (lockStatus) {
                case NO_LOCK:
                case LOCK_EXPIRED:
                    updateMirrorPropertiesImpl(node, assocs);
                    break;
                case LOCK_OWNER:
                    LockType lockType = lockService.getLockType(node);
                    if (lockType != null) {
                        try {
                            lockService.unlock(node, false, true);
                        } catch (Exception e) {
                            throw AlfrescoRuntimeException.create(e, "Unexpected exception during unlock");
                        }
                        updateMirrorPropertiesImpl(node, assocs);
                        lockService.lock(node, lockType);
                    } else {
                        logger.error("Node is locked, but lock type is null: " + node);
                    }
                    break;
                default:
                    logger.error("Can not update index property, because node is locked");
            }

        } finally {
            behaviourFilter.enableBehaviour(node);
        }
    }

    private void updateMirrorPropertiesImpl(NodeRef node, Set<QName> assocs) {

        Map<QName, Serializable> properties = new HashMap<>();
        Map<QName, Serializable> nodeProps = nodeService.getProperties(node);
        List<QName> toRemoveProps = new ArrayList<>();

        for (QName assocQName : assocs) {
            QName propQName = registry.getAssociationIndexProperty(assocQName);

            List<NodeRef> actual = nodeUtils.getAssocTargets(node, assocQName);
            @SuppressWarnings("unchecked")
            List<NodeRef> before = (List<NodeRef>) nodeProps.get(propQName);

            if (!equals(actual, before)) {
                if (actual.size() > 0) {
                    properties.put(propQName, new ArrayList<>(actual));
                } else if (before != null) {
                    toRemoveProps.add(propQName);
                }
            }
        }

        for (QName toRemoveProp : toRemoveProps) {
            nodeService.removeProperty(node, toRemoveProp);
        }

        if (properties.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Update node " + node + " props: " + properties);
            }
            nodeService.addProperties(node, properties);
        }
    }

    private boolean equals(List<NodeRef> first, List<NodeRef> second) {
        if (first == null) {
            first = Collections.emptyList();
        }
        if (second == null) {
            second = Collections.emptyList();
        }
        if (first.size() != second.size()) {
            return false;
        }
        for (NodeRef ref : first) {
            if (second.indexOf(ref) == -1) {
                return false;
            }
        }
        return true;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
        this.behaviourFilter = behaviourFilter;
    }

    public void setRegistry(AssociationIndexPropertyRegistry registry) {
        this.registry = registry;
    }

    public void setTypeQname(String typeQname) {
        this.typeQname = typeQname;
    }
}
