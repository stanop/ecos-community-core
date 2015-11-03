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
package ru.citeck.ecos.form;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Filtering form fields for attorney duplicate form
 * @author deathNC
 */
public class AttorneyDuplicateFormFilter extends AbstractFilter<Object, NodeRef> {

    private static final Log logger = LogFactory.getLog(AttorneyDuplicateFormFilter.class);

    // fields list that will be filtered
    protected Map<String, Map<String, Object>> config = new HashMap<String, Map<String, Object>>();

    //public static final String FIELD_DATA_ASSOC = "assoc_";
    public static final String FIELD_DATA_PROP  = "prop_";
    public static final String FIELD_DATA_ADDED = "_added";
    public static final String FIELD_DATA_REMOVED = "_removed";


    @Override
    public void afterGenerate(Object item, List<String> fields, List<String> forcedFields,
                              Form form, Map<String, Object> context)
    {
        processFilter(form);
        processAssociations(form);
        printMethodName();
    }


    /**
     * This method views the current filter configuration and overrides the fields in
     * form-data that defined in given configuration. Otherwise, if configuration defines
     * fields as null, filter will remove these fields from form-data (not from definition-list).
     * */
    protected void processFilter(Form form) {
        // Getting configuration for current form-QName
        Map<String, Object> fieldMap = config.get(form.getItem().getType());
        if (fieldMap == null) {
            return; // TODO: iterate the parent QName's for detect the given QName
        }

        // Overriding field values by the configuration map
        FormData formData = form.getFormData();
        for (FieldDefinition fieldDef : form.getFieldDefinitions()) {
            if (!fieldMap.containsKey(fieldDef.getName())) continue;

            FormData.FieldData fieldData = formData.getFieldData(fieldDef.getDataKeyName());
            if (fieldData == null) continue;

            Object value = fieldMap.get(fieldDef.getName());
            // if value not defined, get default value for this field
            if (value == null) {
                value = fieldDef.getDefaultValue();
                // if default value is not defined then we remove field from form-data
                if (value == null) {
                    formData.removeFieldData(fieldDef.getDataKeyName());
                    continue;
                }
            }
            formData.addFieldData(fieldData.getName(), value, true);
        }
    }

    /**
     * Removing properties "*_added" and "*_removed". Else adding assocs fields "*_added" and "*_removed".
     * This operations needs for show the "_added" fields.
     * */
    protected void processAssociations(Form form) {
        FormData data = form.getFormData();
        Iterator<String> i = data.getFieldNames().iterator();

        // removing form-fields
        while (i.hasNext()) {
            String name = i.next();
            if ( (name.endsWith(FIELD_DATA_ADDED) || name.endsWith(FIELD_DATA_REMOVED)) && name.startsWith(FIELD_DATA_PROP) ) {
                i.remove();
            }
        }
    }

    /**
     * debug-method
     * */
    private void printMethodName() {
        try {
            Throwable throwable = new Throwable();
            if (logger.isDebugEnabled())
                logger.debug(throwable.getStackTrace()[1].getMethodName());
        } catch (Throwable t) {}
    }

    /**
     * Sets and creates the filter configuration
     * */
    public void setConfig(Map config) {
        this.config = config;
    }



    @Override
    public void beforeGenerate(Object item, List<String> fields, List<String> forcedFields,
                               Form form, Map<String, Object> context)
    {
        printMethodName();
    }

    @Override
    public void beforePersist(Object item, FormData data) {
        // do nothing
    }

    @Override
    public void afterPersist(Object item, FormData data, NodeRef persistedObject) {
        // do nothing
    }
}
