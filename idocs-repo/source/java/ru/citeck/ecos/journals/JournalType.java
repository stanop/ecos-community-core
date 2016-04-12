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
package ru.citeck.ecos.journals;

import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;

public interface JournalType {
    
    public String getId();
    
    public Map<String, String> getOptions();
    
    public List<QName> getAttributes();
    
    public List<QName> getDefaultAttributes();
    
    public List<QName> getVisibleAttributes();
    
    public List<QName> getSearchableAttributes();
    
    public List<QName> getSortableAttributes();
    
    public List<QName> getGroupableAttributes();
    
    public Map<String, String> getAttributeOptions(QName attributeKey);
    
    public boolean isAttributeDefault(QName attributeKey);
    
    public boolean isAttributeVisible(QName attributeKey);
    
    public boolean isAttributeSearchable(QName attributeKey);
    
    public boolean isAttributeSortable(QName attributeKey);
    
    public boolean isAttributeGroupable(QName attributeKey);
    
}
