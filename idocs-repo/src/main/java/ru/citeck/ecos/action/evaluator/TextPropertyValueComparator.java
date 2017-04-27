/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.action.evaluator;

import java.io.Serializable;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.evaluator.compare.PropertyValueComparator;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * Test property value comparator
 * 
 * @author Sergey Tiunov
 */
public class TextPropertyValueComparator implements PropertyValueComparator
{
    
    private static final String MSGID_INVALID_OPERATION = "text_property_value_comparator.invalid_operation";
    private static final String STAR = "*";
    private ComparePropertyValueEvaluator evaluator;
    
    /**
     * @see org.alfresco.repo.action.evaluator.compare.PropertyValueComparator#compare(java.io.Serializable, java.io.Serializable, org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation)
     */
    public boolean compare(
            Serializable propertyValue,
            Serializable compareValue, 
            ComparePropertyValueOperation operation)
    {
        // every check with null would return false
        if(propertyValue == null) return false;
        
        String propertyText = (String) propertyValue;
        String compareText = (String) compareValue;
        
        if (operation == null)
        {
            boolean strictBegin = compareText.startsWith(STAR) == false;
            boolean strictEnd = compareText.endsWith(STAR) == false;
            
            operation = strictBegin ?
                    (strictEnd ? ComparePropertyValueOperation.EQUALS : ComparePropertyValueOperation.BEGINS) :
                    (strictEnd ? ComparePropertyValueOperation.ENDS : ComparePropertyValueOperation.CONTAINS) ;
            
            if(!strictBegin) compareText = compareText.substring(1);
            if(!strictEnd) compareText = compareText.substring(0, (compareText.length()-1));
        }
        
        switch(operation) {
        case EQUALS:
            return compareText.equals(propertyText);
        case BEGINS:
            return propertyText.startsWith(compareText);
        case CONTAINS:
            return propertyText.contains(compareText);
        case ENDS:
            return propertyText.endsWith(compareText);
        case GREATER_THAN:
            return propertyText.compareTo(compareText) > 0;
        case GREATER_THAN_EQUAL:
            return propertyText.compareTo(compareText) >= 0;
        case LESS_THAN:
            return propertyText.compareTo(compareText) < 0;
        case LESS_THAN_EQUAL:
            return propertyText.compareTo(compareText) <= 0;
        default:
            throw new ActionServiceException(
                    MSGID_INVALID_OPERATION, 
                    new Object[]{operation.toString()});
        }
    }
    
    public void setEvaluator(ComparePropertyValueEvaluator evaluator) {
        this.evaluator = evaluator;
    }
    
    public void register() {
        registerComparator(evaluator);
    }

    public void registerComparator(ComparePropertyValueEvaluator evaluator)
    {
        evaluator.registerComparator(DataTypeDefinition.TEXT, this);
        evaluator.registerComparator(DataTypeDefinition.MLTEXT, this);
    }
    
}
