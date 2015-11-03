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
package ru.citeck.ecos.spring;

import java.beans.PropertyEditorSupport;

import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.json.JSONException;
import org.json.JSONObject;

public class RepoUsagePropertyEditor extends PropertyEditorSupport {

    private static final String USERS = "users";
    private static final String DOCUMENTS = "documents";
    private static final String MODE = "mode";
    private static final String EXPIRY_DATE = "expiryDate";

    public void setAsText(String text) {
        Long lastUpdate = null;
        Long users = null;
        Long documents = null;
        LicenseMode licenseMode = LicenseMode.UNKNOWN;
        Long licenseExpiryDate = null;
        boolean readOnly = false;

        JSONObject usage;
        try {
            usage = new JSONObject(text);
            if(usage.has(USERS)) {
                users = usage.getLong(USERS);
            }
            
            if(usage.has(DOCUMENTS)) {
                documents = usage.getLong(DOCUMENTS);
            }
            
            if(usage.has(MODE)) {
                licenseMode = LicenseMode.valueOf(usage.getString(MODE));
            }
            
            if(usage.has(EXPIRY_DATE)) {
                licenseExpiryDate = usage.getLong(EXPIRY_DATE);
            }
            
            if(usage.has("readOnly")) {
                readOnly = usage.getBoolean("readOnly");
            }
            
        } catch (JSONException e) {
            // some error occurred, ignore
        }
        
        setValue(new RepoUsage(lastUpdate, users, documents, licenseMode, licenseExpiryDate, readOnly));
    }
    
}
