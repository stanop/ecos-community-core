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
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.lifecycle.LifeCycleService;
import ru.citeck.ecos.model.LifeCycleModel;

import java.io.Serializable;
import java.util.Map;

/**
 * @author: Alexander Nemerov
 * @date: 07.03.14
 */
public class AutomaticTransitionBehaviour implements
        NodeServicePolicies.OnUpdatePropertiesPolicy{

    private PolicyComponent policyComponent;
    private QName className;
    private NodeService nodeService;
    private LifeCycleService lifeCycleService;

    public void init() {
        bind(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                "onUpdateProperties");
    }

    private void bind(QName policy, String method) {
        policyComponent.bindClassBehaviour(policy, className,
                new JavaBehaviour(this, method,
                    Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef,
                                   Map<QName, Serializable> before,
                                   Map<QName, Serializable> after) {
    	if(!nodeService.exists(nodeRef)) {
    		return;
    	}
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
