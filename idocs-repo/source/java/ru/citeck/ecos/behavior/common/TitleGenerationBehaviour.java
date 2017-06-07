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
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.QName;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.Serializable;
import java.util.*;

/**
 * @author Alexander Nemerov
 * created on 03.03.2015.
 */
public class TitleGenerationBehaviour implements
        NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static final String TITLE_UPDATE_TIME_KEY = TitleGenerationBehaviour.class.toString();

    // common properties
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private NodeService mlAwareNodeService;
    private TemplateService templateService;

    // distinct properties
    private QName className;
    private String nodeVariable;
    private String templateEngine;
    private Map<Locale, String> titleMLTemplate = new HashMap<>();
    private Map<Locale, String> descriptionMLTemplate = new HashMap<>();
	private int order = 80;

    public void init() {
		OrderedBehaviour updateBehaviour = new OrderedBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT, order);
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, className, updateBehaviour);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before,
                                   Map<QName, Serializable> after) {
        updateName(nodeRef);
    }

    private void updateName(final NodeRef nodeRef) {

        if (!nodeService.exists(nodeRef)) {
            return;
        }

        Map<NodeRef, Date> updateTime = TransactionalResourceHelper.getMap(TITLE_UPDATE_TIME_KEY);
        Date updated = updateTime.get(nodeRef);
        Date modified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);

        if (modified == null || updated == null || modified.after(updated)) {
            setProperty(nodeRef, ContentModel.PROP_TITLE, titleMLTemplate);
            setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, descriptionMLTemplate);
            updateTime.put(nodeRef, modified);
        }
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
                             final Map<Locale, String> mlTemplate) {

        MLText mlValue = getProcessedMLText(nodeRef, mlTemplate);
        if(mlValue != null && mlValue.size() > 0) {
            setProperty(nodeRef, property, mlValue);
        }
    }

    private Void setProperty(final NodeRef nodeRef, final QName property, final MLText value) {
        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            public Void doWork() throws Exception {
                Serializable currentValue = mlAwareNodeService.getProperty(nodeRef, property);
                if (!Objects.equals(value, currentValue)) {
                    nodeService.setProperty(nodeRef, property, value);
                }
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

    public void setMlAwareNodeService(NodeService mlAwareNodeService) {
        this.mlAwareNodeService = mlAwareNodeService;
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
        titleMLTemplate.put(I18NUtil.getContentLocale(), titleTemplate);
    }

    public void setTitleMLTemplate(Map<Locale, String> titleTemplate) {
        this.titleMLTemplate.putAll(titleTemplate);
    }

    public void setDescriptionTemplate(String descriptionTemplate) {
        descriptionMLTemplate.put(I18NUtil.getContentLocale(), descriptionTemplate);
    }

    public void setDescriptionMLTemplate(Map<Locale, String> descriptionTemplate) {
        this.descriptionMLTemplate.putAll(descriptionTemplate);
    }
    public void setOrder(int order) {
        this.order = order;
    }
}
