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
package ru.citeck.ecos.behavior.lifecycle;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.lifecycle.LifeCycleService;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.model.LifeCycleModel;

/**
 * @author: Alexander Nemerov
 * @date: 26.02.14
 */
public class AddDocumentStateBehaviour implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnAddAspectPolicy {

    private PolicyComponent policyComponent;
    private QName className;

    private NodeService nodeService;
    private LifeCycleService lifeCycleService;

    private final String START_STATE = "start";

    public void init() {
        bind(NodeServicePolicies.OnCreateNodePolicy.QNAME, "onCreateNode");
        bind(NodeServicePolicies.OnAddAspectPolicy.QNAME, "onAddAspect");
    }

    private void bind(QName policy, String method) {
        policyComponent.bindClassBehaviour(policy, className,
                new JavaBehaviour(this, method,
                        Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        startLifecycle(childAssocRef.getChildRef());
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectName) {
        if(!IdocsModel.ASPECT_LIFECYCLE.equals(aspectName))
            return;
        startLifecycle(nodeRef);
    }

    private void startLifecycle(final NodeRef nodeRef) {
        if(!nodeService.exists(nodeRef)) {
            return;
        }

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                nodeService.setProperty(nodeRef,
                        LifeCycleModel.PROP_STATE, START_STATE);
                return null;
            }
        });
        boolean transited = lifeCycleService.doTransition(nodeRef, LifeCycleModel.CONSTR_AUTOMATIC_TRANSITION);
        if(!transited) {
            lifeCycleService.doTimerTransition(nodeRef);
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }
}
