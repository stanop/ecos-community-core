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
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.QName;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alexander Nemerov
 * created on 03.03.2015.
 */
public class TitleGenerationBehaviour implements
        NodeServicePolicies.OnUpdatePropertiesPolicy,
        VersionServicePolicies.OnCreateVersionPolicy {

    // common properties
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private TemplateService templateService;

    // distinct properties
    private QName className;
    private String nodeVariable;
    private String templateEngine;
    private String titleTemplate;
    private Map<Locale, String> titleMLTemplate;
    private String descriptionTemplate;
    private Map<Locale, String> descriptionMLTemplate;
	private int order = 80;

    public void init() {
        //policyComponent.bindClassBehaviour(NodeServicePolicies
        //        .OnUpdatePropertiesPolicy.QNAME, className, new JavaBehaviour(
        //        this, "onUpdateProperties", Behaviour.NotificationFrequency.EVERY_EVENT));
		OrderedBehaviour updateBehaviour = new OrderedBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT, order);
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, className, updateBehaviour);
		
		OrderedBehaviour createBehaviour = new OrderedBehaviour(this, "onCreateVersion", NotificationFrequency.TRANSACTION_COMMIT, order);
		policyComponent.bindClassBehaviour(VersionServicePolicies.OnCreateVersionPolicy.QNAME, className, createBehaviour);

        //policyComponent.bindClassBehaviour(VersionServicePolicies
        //        .OnCreateVersionPolicy.QNAME, className, new JavaBehaviour(
        //        this, "onCreateVersion", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before,
                                   Map<QName, Serializable> after) {
        updateName(nodeRef);
    }

    @Override
    public void onCreateVersion(QName qName, NodeRef nodeRef,
                                Map<String, Serializable> map, PolicyScope policyScope) {
        updateName(nodeRef);
    }

    private void updateName(final NodeRef nodeRef) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }

        setProperty(nodeRef, ContentModel.PROP_TITLE, titleMLTemplate, titleTemplate);
        setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, descriptionMLTemplate, descriptionTemplate);
    }

    private MLText getProcessedMLText(final NodeRef nodeRef, final Map<Locale, String> mlText) {
        if(mlText == null) return null;
        MLText result = new MLText();
        for(Map.Entry<Locale, String> entry : mlText.entrySet()) {
            Locale locale = entry.getKey();
            String template = entry.getValue();
            if(locale != null && template != null) {
                String processedString = getProcessedString(nodeRef, template);
                if(processedString != null && !processedString.isEmpty()) {
                    result.put(locale, processedString);
                }
            }
        }
        return result;
    }

    private String getProcessedString(final NodeRef nodeRef, final String template) {
        if (template == null) {return null;}
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<String>() {
            @Override
            public String doWork() throws Exception {
                HashMap<String, Object> model = new HashMap<String, Object>(1);
                model.put(nodeVariable, nodeRef);
                return templateService.processTemplateString(templateEngine, template, model);
            }
        });
    }

    private void setProperty(final NodeRef nodeRef, final QName property,
                             final Map<Locale, String> mlTemplate, final String template) {

        MLText mlValue = getProcessedMLText(nodeRef, mlTemplate);
        if(mlValue != null && mlValue.size() > 0) {
            setProperty(nodeRef, property, mlValue);
        } else {
            String value = getProcessedString(nodeRef, template);
            if(value != null && !value.isEmpty()) {
                setProperty(nodeRef, property, value);
            }
        }
    }

    private Void setProperty(final NodeRef nodeRef, final QName property, final Serializable value) {
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

    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }

    public void setTitleMLTemplate(Map<Locale, String> titleTemplate) {
        this.titleMLTemplate = titleTemplate;
    }

    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public void setDescriptionMLTemplate(Map<Locale, String> descriptionTemplate) {
        this.descriptionMLTemplate = descriptionTemplate;
    }
}
