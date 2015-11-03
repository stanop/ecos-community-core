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
import java.util.Date;
import java.util.TimeZone;

import org.alfresco.util.ISO8601DateFormat;

public class DatePropertyEditor extends PropertyEditorSupport {
    
    @Override
    public void setAsText(String text) {
        try {
            Date date = null;
            if(ISO8601DateFormat.isTimeComponentDefined(text)) {
                date = ISO8601DateFormat.parse(text);
            } else {
                date = ISO8601DateFormat.parseDayOnly(text, TimeZone.getDefault());
            }
            setValue(date);
        } catch (RuntimeException e) {
            // set nothing
        }
    }
}
