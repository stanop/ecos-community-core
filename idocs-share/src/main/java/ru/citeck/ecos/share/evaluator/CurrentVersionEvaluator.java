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
package ru.citeck.ecos.share.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;

/**
 * Author: alexander.nemerov
 * Date: 08.10.13
 */
public class CurrentVersionEvaluator extends BaseEvaluator {

    private static final String currentVersionAccessor = "node.properties.cm:versionLabel";
    private String versionsAccessor;

    @Override
    public boolean evaluate(JSONObject object) {

        // deserialize confirm decisions:
        JSONObject decisions = ConfirmUtil.deserializeVersions(object, this, versionsAccessor);

        // current user
        RequestContext rc = ThreadLocalRequestContext.getRequestContext();
        String userName = rc.getUserId();

        // look for his confirm decision
        Object decisionVersion = this.getJSONValue(decisions, userName + ".versionLabel");

        // get current version of document
        Object currentVersion = this.getJSONValue(object, currentVersionAccessor);

        return decisionVersion != null && currentVersion != null && decisionVersion.equals(currentVersion);
    }

    public void setVersionsAccessor(String versionsAccessor) {
        this.versionsAccessor = versionsAccessor;
    }
}
