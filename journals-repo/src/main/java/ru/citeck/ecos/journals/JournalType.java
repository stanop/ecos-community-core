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

    String getDataSource();

    String getId();
    
    Map<String, String> getOptions();
    
    List<QName> getAttributes();
    
    List<QName> getDefaultAttributes();
    
    List<QName> getVisibleAttributes();
    
    List<QName> getSearchableAttributes();
    
    List<QName> getSortableAttributes();
    
    List<QName> getGroupableAttributes();
    
    Map<String, String> getAttributeOptions(QName attributeKey);
    
    boolean isAttributeDefault(QName attributeKey);
    
    boolean isAttributeVisible(QName attributeKey);
    
    boolean isAttributeSearchable(QName attributeKey);
    
    boolean isAttributeSortable(QName attributeKey);
    
    boolean isAttributeGroupable(QName attributeKey);

    List<JournalBatchEdit> getBatchEdit(QName attributeKey);

    List<JournalGroupAction> getGroupActions();

    JournalCriterion getCriterion(QName attributeKey);
    
    String getFieldsSchema();
    
}
