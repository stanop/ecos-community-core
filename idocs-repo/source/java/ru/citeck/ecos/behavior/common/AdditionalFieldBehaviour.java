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

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import ru.citeck.ecos.webscripts.database.SQLSelectExecutor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;

/**
 * Changes value of additional field when main field value is changed.
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class AdditionalFieldBehaviour implements OnUpdatePropertiesPolicy {

    private PolicyComponent policyComponent;

    private NodeService nodeService;

    private QName className;

    private QName mainField;

    private QName additionalField;

    private SQLSelectExecutor queryExecutor;

    private String queryParameterName;

    private String resultFieldName;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setMainField(QName mainField) {
        this.mainField = mainField;
    }

    public void setAdditionalField(QName additionalField) {
        this.additionalField = additionalField;
    }

    public void setQueryExecutor(SQLSelectExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    public void setQueryParameterName(String queryParameterName) {
        this.queryParameterName = queryParameterName;
    }

    public void setResultFieldName(String resultFieldName) {
        this.resultFieldName = resultFieldName;
    }

    public void init() {
        policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, className,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                if (!nodeService.exists(nodeRef)) {
                    return null;
                }
                Object oldValue = before.get(mainField);
                Object newValue = after.get(mainField);
                if (oldValue == null && newValue == null) {
                    return null;
                } else if (oldValue != null && newValue == null) {
                    updateAdditionalField("", nodeRef);
                }  else if (!newValue.equals(oldValue)) {
                    updateAdditionalField(newValue.toString(), nodeRef);
                }
                return null;
            }
        });
    }

    private void updateAdditionalField(String newValue, NodeRef nodeRef) throws JSONException {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(queryParameterName, newValue);
        JSONObject json = queryExecutor.executeQuery(parameters);
        String additionalFieldValue = json.getJSONArray(SQLSelectExecutor.RESULT).getJSONObject(0).getString(resultFieldName);
        nodeService.setProperty(nodeRef, additionalField, additionalFieldValue);
    }
}
