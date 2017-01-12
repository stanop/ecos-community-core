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
package ru.citeck.ecos.lifecycle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.Association;
import org.alfresco.repo.jscript.ChildAssociation;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.authority.script.ScriptUser;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

/**
 * Extended version of org.alfresco.repo.jscript.ValueConverter.
 * We couldn't extend it, because method was final.
 * 
 * @author Sergey Tiunov
 *
 */
public class LifeCycleValueConverter {
    
    private static final String TYPE_DATE = "Date";
    
    public final Object convertValueForJava(Object value)
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof ScriptNode)
        {
            // convert back to NodeRef
            value = ((ScriptNode)value).getNodeRef();
        }
        else if (value instanceof ScriptGroup)
        {
            value = ((ScriptGroup)value).getGroupNodeRef();
        }
        else if (value instanceof ScriptUser)
        {
            value = ((ScriptUser)value).getPersonNodeRef();
        }
        else if (value instanceof ChildAssociation)
        {
               value = ((ChildAssociation)value).getChildAssociationRef();
        }
        else if (value instanceof Association)
        {
               value = ((Association)value).getAssociationRef();
        }
        else if (value instanceof Wrapper)
        {
            // unwrap a Java object from a JavaScript wrapper
            // recursively call this method to convert the unwrapped value
            value = convertValueForJava(((Wrapper)value).unwrap());
        }
        else if (value instanceof Scriptable)
        {
            // a scriptable object will probably indicate a multi-value property
            // set using a JavaScript Array object
            Scriptable values = (Scriptable)value;
            
            if (value instanceof IdScriptableObject)
            {
                // TODO: add code here to use the dictionary and convert to correct value type
                if (TYPE_DATE.equals(((IdScriptableObject)value).getClassName()))
                {
                    value = Context.jsToJava(value, Date.class);
                }
                else if (value instanceof NativeArray)
                {
                    // convert JavaScript array of values to a List of objects
                    Object[] propIds = values.getIds();
                    if (isArray(propIds) == true)
                    {                    
                        List<Object> propValues = new ArrayList<Object>(propIds.length);
                        for (int i=0; i<propIds.length; i++)
                        {
                            // work on each key in turn
                            Object propId = propIds[i];
                            
                            // we are only interested in keys that indicate a list of values
                            if (propId instanceof Integer)
                            {
                                // get the value out for the specified key
                                Object val = values.get((Integer)propId, values);
                                // recursively call this method to convert the value
                                propValues.add(convertValueForJava(val));
                            }
                        }

                        value = propValues;
                    }
                    else
                    {
                        Map<Object, Object> propValues = new HashMap<Object, Object>(propIds.length);
                        for (Object propId : propIds)
                        {
                            // Get the value and add to the map
                            Object val = values.get(propId.toString(), values);
                            propValues.put(convertValueForJava(propId), convertValueForJava(val));
                        }
                        
                        value = propValues;
                    }
                }
                else
                {
                    // convert Scriptable object of values to a Map of objects
                    Object[] propIds = values.getIds();
                    Map<String, Object> propValues = new HashMap<String, Object>(propIds.length);
                    for (int i=0; i<propIds.length; i++)
                    {
                        // work on each key in turn
                        Object propId = propIds[i];

                        // we are only interested in keys that indicate a list of values
                        if (propId instanceof String)
                        {
                            // get the value out for the specified key
                            Object val = values.get((String)propId, values);
                            // recursively call this method to convert the value
                            propValues.put((String)propId, convertValueForJava(val));
                        }
                    }
                    value = propValues;
                }
            }
            else
            {
                // convert Scriptable object of values to a Map of objects
                Object[] propIds = values.getIds();
                Map<String, Object> propValues = new HashMap<String, Object>(propIds.length);
                for (int i=0; i<propIds.length; i++)
                {
                    // work on each key in turn
                    Object propId = propIds[i];

                    // we are only interested in keys that indicate a list of values
                    if (propId instanceof String)
                    {
                        // get the value out for the specified key
                        Object val = values.get((String)propId, values);
                        // recursively call this method to convert the value
                        propValues.put((String)propId, convertValueForJava(val));
                    }
                }
                value = propValues;
            }
        }
        else if (value.getClass().isArray())
        {
            // convert back a list of Java values
            int length = Array.getLength(value);
            ArrayList<Object> list = new ArrayList<Object>(length);
            for (int i=0; i<length; i++)
            {
                list.add(convertValueForJava(Array.get(value, i)));
            }
            value = list;
        }
        else if (value instanceof Collection)
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Collection<Object> originalItems = (Collection) value;
            Collection<Object> convertedItems = new ArrayList<Object>(originalItems.size());
            for(Object item : originalItems) {
                convertedItems.add(this.convertValueForJava(item));
            }
            value = convertedItems;
        }
        return value;
    }
    
    private boolean isArray(Object[] ids)
    {
        boolean result = true;
        for (Object id : ids)
        {
            if (id instanceof Integer == false)
            {
               result = false;
               break;
            }
        }
        return result;
    }
    
}
