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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.search.AssociationIndexPropertyRegistry;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//TODO: remove after AssociationIndexins will be stable
public class AssociationIndexingOld implements OnCreateAssociationPolicy,
                                               OnDeleteAssociationPolicy {
    private static Log logger = LogFactory.getLog(AssociationIndexing.class);

    private NodeService nodeService;
    private LockService lockService;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private AssociationIndexPropertyRegistry registry;
    private String typeQname;

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

    @Override
    public void onDeleteAssociation(AssociationRef nodeAssocRef) {
        NodeRef node = nodeAssocRef.getSourceRef();
        if (!nodeService.exists(node)) {
            return;
        }
        update(node, nodeAssocRef.getTypeQName());
    }

    @Override
    public void onCreateAssociation(AssociationRef nodeAssocRef) {
        NodeRef node = nodeAssocRef.getSourceRef();
        if (!nodeService.exists(node)) {
            return;
        }
        update(node, nodeAssocRef.getTypeQName());
    }

    private void update(final NodeRef node, final QName assocQName) {
        AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () -> {
            updateAssociationMirrorProperty(node, assocQName);
            return null;
        });
    }

    public void updatePropertiesOnFullPersistedNodes(NodeRef node, QName assocQName, List<NodeRef> nodeRefs) {
        if (!nodeService.exists(node)) {
            return;
        }
        if (nodeRefs == null) {
            nodeRefs = new ArrayList<>();
        }
        List<NodeRef> finalNodeRefs = nodeRefs;
        AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () -> {
            updateMirrorProperties(node, assocQName, finalNodeRefs);
            return null;
        });

    }

    private void updateAssociationMirrorProperty(NodeRef node, QName assocQName) {
        // get nodeRefs
        List<NodeRef> nodeRefs = new ArrayList<>();
        List<AssociationRef> assocs = nodeService.getTargetAssocs(node, assocQName);
        for (AssociationRef assoc : assocs) {
            nodeRefs.add(assoc.getTargetRef());
        }

        updateMirrorProperties(node, assocQName, nodeRefs);
    }

    private void updateMirrorProperties(NodeRef node, QName assocQName, List<NodeRef> nodeRefs) {
        QName propQName = registry.getAssociationIndexProperty(assocQName);

        try {
            behaviourFilter.disableBehaviour(node);

            LockStatus lockStatus = lockService.getLockStatus(node);
            switch (lockStatus) {
                case NO_LOCK:
                case LOCK_EXPIRED:
                    setIndexProperty(node, propQName, nodeRefs);
                    break;
                case LOCK_OWNER:
                    LockType lockType = lockService.getLockType(node);
                    if (lockType != null) {
                        try {
                            // new method not present in 4.2.c: unlock(nodeRef, unlockChildren, allowCheckedOut)
                            Method unlock = LockService.class.getMethod("unlock", NodeRef.class, boolean.class, boolean.class);
                            unlock.invoke(lockService, node, false, true);
                        } catch (NoSuchMethodException e) {
                            lockService.unlock(node);
                        } catch (Exception e) {
                            throw AlfrescoRuntimeException.create(e, "Unexpected exception during unlock");
                        }
                        setIndexProperty(node, propQName, nodeRefs);
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

    private void setIndexProperty(NodeRef node, QName propQName, List<NodeRef> nodeRefs) {
        if (nodeRefs.isEmpty()) {
            nodeService.removeProperty(node, propQName);
        } else {
            if (logger.isDebugEnabled()) {
                StringBuilder debugMessage = new StringBuilder();
                debugMessage.append("setIndexProperty...");
                debugMessage.append("\nnode: ").append(node);
                debugMessage.append("\nproperty QName: ").append(propQName);
                debugMessage.append("\nnode refs list: ").append(nodeRefs);
                logger.debug(debugMessage);
            }
            nodeService.setProperty(node, propQName, (Serializable) nodeRefs);
        }
    }

}
