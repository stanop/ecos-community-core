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

import ru.citeck.ecos.utils.ConvertUtils;

/**
 * Test property value comparator
 * 
 * @author Sergey Tiunov
 */
public class BooleanPropertyValueComparator implements PropertyValueComparator
{
    
    private static final String MSGID_INVALID_OPERATION = "numeric_property_value_comparator.invalid_operation";
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
        
        Boolean propertyBool = ConvertUtils.convertSingleValue(propertyValue, Boolean.class);
        Boolean compareBool = ConvertUtils.convertSingleValue(compareValue, Boolean.class);
        
        switch(operation) {
        case EQUALS:
            return compareBool.equals(propertyBool);
        case GREATER_THAN:
            return propertyBool.compareTo(compareBool) > 0;
        case GREATER_THAN_EQUAL:
            return propertyBool.compareTo(compareBool) >= 0;
        case LESS_THAN:
            return propertyBool.compareTo(compareBool) < 0;
        case LESS_THAN_EQUAL:
            return propertyBool.compareTo(compareBool) <= 0;
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
        evaluator.registerComparator(DataTypeDefinition.BOOLEAN, this);
    }
    
}
