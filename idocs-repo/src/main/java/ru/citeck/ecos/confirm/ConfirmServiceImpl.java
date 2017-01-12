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
package ru.citeck.ecos.confirm;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.citeck.ecos.confirm.ConfirmConfigService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Author: alexander.nemerov
 * Date: 30.09.13
 */
public class ConfirmServiceImpl implements ConfirmService {

    private ConfirmConfigService confirmConfigService;
    private AuthenticationService authenticationService;
    private VersionService versionService;
    private NodeService nodeService;
    private NamespacePrefixResolver namespaceService;

    @Override
    public void setDecision(final String decision, final String versionLabel,
                            final NodeRef nodeRef) throws ConfirmException {
        List<String> allowedDecisions = confirmConfigService.getAllowedDecisions();

        if (!allowedDecisions.contains(decision)) {
            throw new ConfirmException("There is no " + decision + "" +
                    "in allowedDecisions list");
        }

        final String currentUser = authenticationService.getCurrentUserName();

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                confirm(decision, versionLabel, nodeRef, currentUser);
                return null;
            }
        });

    }

    @Override
    public String getDecision(String versionLabel, NodeRef nodeRef) throws JSONException {
        QName consideredVersionsQName = QName.createQName("wfcf", "consideredVersions", namespaceService);
        String prop = (String) nodeService.getProperty(nodeRef, consideredVersionsQName);
        if (prop == null) {
            return null;
        }
        JSONObject consideredVersions = deserialize(prop);
        Version version = versionService.getVersionHistory(nodeRef).getVersion(versionLabel);
        String versionRef = version.getFrozenStateNodeRef().toString();
        JSONArray names = consideredVersions.names();
        for (int i = 0; i < consideredVersions.length(); i++) {
            JSONObject record = consideredVersions.getJSONObject(names.getString(i));
            if(record.getString("user").equals(authenticationService.getCurrentUserName())
                    && record.getString("versionLabel").equals(versionLabel)
                    && record.getString("versionRef").equals(versionRef)) {
                return record.getString("decision");
            }
        }
        return null;
    }

    @Override
    public void addConsiderableVersion(String user, final String versionLabel, final NodeRef nodeRef) throws JSONException {
	    setVersionProperties(nodeRef);
	    final QName considerableVersionsQName = QName.createQName("wfcf", "considerableVersions", namespaceService);
        String prop = (String) nodeService.getProperty(nodeRef, considerableVersionsQName);
        final JSONObject considerableVersions = prop != null ? deserialize(prop) : new JSONObject();
        if (considerableVersions.opt(user) != null) {return;}
        JSONObject versionInfo = new JSONObject();
        Version version = versionService.getVersionHistory(nodeRef).getVersion(versionLabel);
        versionInfo.put("user", user);
        versionInfo.put("versionRef", version.getFrozenStateNodeRef());
        versionInfo.put("versionLabel", versionLabel);
        considerableVersions.put(user, versionInfo);
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                nodeService.setProperty(nodeRef, considerableVersionsQName, serialize(considerableVersions));
                return null;
            }
        });
    }

    @Override
    public boolean isConsiderableVersion(String user, String versionLabel, NodeRef nodeRef) throws JSONException {
        final QName considerableVersionsQName = QName.createQName("wfcf", "considerableVersions", namespaceService);
        String prop = (String) nodeService.getProperty(nodeRef, considerableVersionsQName);
        final JSONObject considerableVersions = prop != null ? deserialize(prop) : new JSONObject();
        JSONObject record = considerableVersions.getJSONObject(user);
        return record.getString("versionLabel").equals(versionLabel);
    }

    @Override
    public void updateConsiderable(final NodeRef nodeRef) throws JSONException {
	    setVersionProperties(nodeRef);
        Version version = versionService.getVersionHistory(nodeRef).getHeadVersion();
        final QName considerableVersionsQName = QName.createQName("wfcf", "considerableVersions", namespaceService);
        String prop = (String) nodeService.getProperty(nodeRef, considerableVersionsQName);
        final JSONObject considerableVersions = prop != null ? deserialize(prop) : new JSONObject();
        JSONObject versionInfo = new JSONObject();
        final String user = authenticationService.getCurrentUserName();
        versionInfo.put("user", user);
        versionInfo.put("versionRef", version.getFrozenStateNodeRef());
        versionInfo.put("versionLabel", version.getVersionLabel());
        considerableVersions.put(user, versionInfo);
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                nodeService.setProperty(nodeRef, considerableVersionsQName, serialize(considerableVersions));
                return null;
            }
        });

    }

	@Override
	public void addCurrentVersionToConsiderable(String user, NodeRef nodeRef) throws JSONException {
		setVersionProperties(nodeRef);
		String versionLabel = versionService.getCurrentVersion(nodeRef).getVersionLabel();
		addConsiderableVersion(user, versionLabel, nodeRef);
	}

	private void confirm(String decision, String versionLabel, NodeRef nodeRef,
                         String currentUser) throws JSONException {
	    setVersionProperties(nodeRef);
        String confirmVersionLabel;
        if(versionLabel == null) {
            confirmVersionLabel = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
        } else {
            confirmVersionLabel = versionLabel;
        }
        Version version = versionService.getVersionHistory(nodeRef).getVersion(confirmVersionLabel);
        QName consideredVersionsQName = QName.createQName("wfcf", "consideredVersions", namespaceService);
        String prop = (String) nodeService.getProperty(nodeRef, consideredVersionsQName);
        JSONObject consideredVersions = prop != null ? deserialize(prop) : new JSONObject();
        JSONObject versionInfo = new JSONObject();
        versionInfo.put("user", currentUser);
        versionInfo.put("versionRef", version.getFrozenStateNodeRef());
        versionInfo.put("versionLabel", confirmVersionLabel);
        versionInfo.put("decision", decision);

        consideredVersions.put(currentUser, versionInfo);

        nodeService.setProperty(nodeRef, consideredVersionsQName, serialize(consideredVersions));
    }

	private void setVersionProperties(final NodeRef nodeRef) {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
			public Object doWork() throws Exception {
				Map<QName, Serializable> versionProperties = new HashMap<QName, Serializable>();
				versionProperties.put(ContentModel.PROP_AUTO_VERSION, true);
				versionProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				versionService.ensureVersioningEnabled(nodeRef, versionProperties);
				return null;
			}
		});
	}

	private String serialize(JSONObject consideredVersions) throws JSONException {
        String records = "";
        List<String> partNames = confirmConfigService.getParts();
        for (int i = 0; i < consideredVersions.length(); i++) {
            String parts = "";
            JSONArray names = consideredVersions.names();
            int innerObjectLength = consideredVersions.getJSONObject(names.getString(i)).length();
            int length = (partNames.size() <= innerObjectLength) ? partNames.size() : innerObjectLength;
            for (int j = 0; j < length; j++) {
                parts += consideredVersions.getJSONObject(names.getString(i)).getString(partNames.get(j));
                if (j != length - 1) {
                    parts += confirmConfigService.getPartSeparator();
                }
            }
            records += parts;
            if (i != consideredVersions.length() - 1) {
                records += confirmConfigService.getRecordSeparator();
            }
        }
        return records;
    }

    private JSONObject deserialize(String prop) throws JSONException {
        JSONObject result = new JSONObject();
        String[] records = prop.split(Pattern.quote(confirmConfigService.getRecordSeparator()));
        List<String> partNames = confirmConfigService.getParts();
        for (String record : records) {
            JSONObject jsonRecord = new JSONObject();
            String[] parts = record.split(Pattern.quote(confirmConfigService.getPartSeparator()));
            for (int i = 0; i < partNames.size() && i< parts.length; i++) {
                jsonRecord.put(partNames.get(i), parts[i]);
            }
            result.put(parts[0], jsonRecord);
        }
        return result;
    }

    public void setConfirmConfigService(ConfirmConfigService confirmConfigService) {
        this.confirmConfigService = confirmConfigService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespacePrefixResolver namespaceService) {
        this.namespaceService = namespaceService;
    }
}
