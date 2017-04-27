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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Nemerov
 *         created on 20.04.2015.
 */
public class FieldAutoFillBehaviour implements
        NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static Log logger = LogFactory.getLog(FieldAutoFillBehaviour.class);

    // common properties
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private TemplateService templateService;

    // distinct properties
    private QName className;
    private String nodeVariable;
    private String templateEngine;


    private String accessorTemplate;
    private QName fieldForFill;
    private Map<String, String> valueMap;
    private int order = 65;

    public void init() {
//        OrderedBehaviour updateBehaviour = new OrderedBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT, order);
        JavaBehaviour updateBehaviour = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, className, updateBehaviour);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }

        String accessorValue = getObjectFromTemplate(nodeRef, accessorTemplate);
        if(accessorValue == null) {
            logger.error("Accessor value is null");
            return;
        }
        if(valueMap == null || valueMap.isEmpty()) {
            setProperty(nodeRef, fieldForFill, accessorValue);
        } else if (valueMap.containsKey(accessorValue)){
            setProperty(nodeRef, fieldForFill, valueMap.get(accessorValue));
        }
    }

    private String getObjectFromTemplate(final NodeRef nodeRef, final String template) {
        if (template == null) {return null;}
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<String>() {
            @Override
            public String doWork() throws Exception {
                HashMap<String, Object> model = new HashMap<>(1);
                model.put(nodeVariable, nodeRef);
                return templateService.processTemplateString(templateEngine, template, model);
            }
        });
    }

    private Void setProperty(final NodeRef nodeRef, final QName property, final String value) {
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            public Void doWork() throws Exception {
                nodeService.setProperty(nodeRef, property, value);
                return null;
            }
        });
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setNodeVariable(String nodeVariable) {
        this.nodeVariable = nodeVariable;
    }

    public void setTemplateEngine(String templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void setAccessorTemplate(String accessorTemplate) {
        this.accessorTemplate = accessorTemplate;
    }

    public void setFieldForFill(QName fieldForFill) {
        this.fieldForFill = fieldForFill;
    }

    public void setValueMap(Map<String, String> valueMap) {
        this.valueMap = valueMap;
    }
}
