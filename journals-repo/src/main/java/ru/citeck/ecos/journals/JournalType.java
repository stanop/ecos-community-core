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

public interface JournalType {

    String getDataSource();

    String getId();
    
    Map<String, String> getOptions();
    
    List<String> getAttributes();
    
    List<String> getDefaultAttributes();
    
    List<String> getVisibleAttributes();
    
    List<String> getSearchableAttributes();
    
    List<String> getSortableAttributes();
    
    List<String> getGroupableAttributes();
    
    Map<String, String> getAttributeOptions(String attributeKey);
    
    boolean isAttributeDefault(String attributeKey);
    
    boolean isAttributeVisible(String attributeKey);
    
    boolean isAttributeSearchable(String attributeKey);
    
    boolean isAttributeSortable(String attributeKey);
    
    boolean isAttributeGroupable(String attributeKey);

    List<JournalBatchEdit> getBatchEdit(String attributeKey);

    List<JournalGroupAction> getGroupActions();

    JournalCriterion getCriterion(String attributeKey);
}
