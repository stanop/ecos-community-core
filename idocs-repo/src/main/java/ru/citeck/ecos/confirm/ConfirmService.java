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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.json.JSONException;

/**
 * Author: alexander.nemerov
 * Date: 30.09.13
 */
public interface ConfirmService {

    void setDecision(String decision, String versionLabel, NodeRef nodeRef) throws ConfirmException;

    String getDecision(String versionLabel, NodeRef nodeRef) throws JSONException;

    void addConsiderableVersion(String user, String versionLabel, NodeRef nodeRef) throws JSONException;

    boolean isConsiderableVersion(String user, String versionLabel, NodeRef nodeRef) throws JSONException;

    void updateConsiderable(NodeRef nodeRef) throws JSONException;

	void addCurrentVersionToConsiderable(String user, NodeRef nodeRef) throws JSONException;
}
