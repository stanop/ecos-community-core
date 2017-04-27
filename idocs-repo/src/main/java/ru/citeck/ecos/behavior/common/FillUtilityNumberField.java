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

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

/**
 * @author Alexander Nemerov <alexander.nemerov@citeck.ru>
 * date: 06.05.14
 */
public class FillUtilityNumberField implements NodeServicePolicies.OnUpdatePropertiesPolicy{

    private PolicyComponent policyComponent;
    private QName className;
    private QName stringField;
    private QName numericField;
    private NodeService nodeService;

    private static final Log logger = LogFactory.getLog(FillUtilityNumberField.class);

    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, className,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if(logger.isDebugEnabled()) {
            logger.debug("onUpdateProperties event");
        }
        if(!nodeService.exists(nodeRef)) {return;}
        String stringFieldAfter = (String) after.get(stringField);
        try {
            if(stringFieldAfter != null) {
                String onlyNumberString = stringFieldAfter.replaceAll("[^0-9]", "");
                BigInteger numericFieldAfter = new BigInteger(onlyNumberString);
                nodeService.setProperty(nodeRef, numericField, numericFieldAfter);
            } else {
                nodeService.setProperty(nodeRef, numericField, null);
            }
        } catch (NumberFormatException e) {
            logger.error("Failed convert " + stringFieldAfter + " to numeric", e);
        }

    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setStringField(QName stringField) {
        this.stringField = stringField;
    }

    public void setNumericField(QName numericField) {
        this.numericField = numericField;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
