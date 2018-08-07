package ru.citeck.ecos.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

/**
 * Value conversion allowing safe usage of values in Script and Java.
 */
public class ValueConverter {

    private static final String TYPE_DATE = "Date";

    public static Object convertValueForJava(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Wrapper) {
            // unwrap a Java object from a JavaScript wrapper
            // recursively call this method to convert the unwrapped value
            value = convertValueForJava(((Wrapper) value).unwrap());
        } else if (value instanceof Scriptable) {
            // a scriptable object will probably indicate a multi-value property
            // set using a JavaScript Array object
            Scriptable values = (Scriptable) value;

            if (value instanceof IdScriptableObject) {
                // TODO: add code here to use the dictionary and convert to correct value type
                if (TYPE_DATE.equals(((IdScriptableObject) value).getClassName())) {
                    value = Context.jsToJava(value, Date.class);
                } else if (value instanceof NativeArray) {
                    // convert JavaScript array of values to a List of objects
                    Object[] propIds = values.getIds();
                    if (isArray(propIds)) {
                        List<Object> propValues = new ArrayList<>(propIds.length);
                        for (Object propId : propIds) {
                            // work on each key in turn
                            // we are only interested in keys that indicate a list of values
                            if (propId instanceof Integer) {
                                // get the value out for the specified key
                                Object val = values.get((Integer) propId, values);
                                // recursively call this method to convert the value
                                propValues.add(convertValueForJava(val));
                            }
                        }

                        value = propValues;
                    } else {
                        Map<Object, Object> propValues = new HashMap<>(propIds.length);
                        for (Object propId : propIds) {
                            // Get the value and add to the map
                            Object val = values.get(propId.toString(), values);
                            propValues.put(convertValueForJava(propId), convertValueForJava(val));
                        }

                        value = propValues;
                    }
                } else {
                    // convert Scriptable object of values to a Map of objects
                    Object[] propIds = values.getIds();
                    Map<String, Object> propValues = new HashMap<String, Object>(propIds.length);
                    for (Object propId : propIds) {
                        // work on each key in turn
                        // we are only interested in keys that indicate a list of values
                        if (propId instanceof String) {
                            // get the value out for the specified key
                            Object val = values.get((String) propId, values);
                            // recursively call this method to convert the value
                            propValues.put((String) propId, convertValueForJava(val));
                        }
                    }
                    value = propValues;
                }
            } else {
                // convert Scriptable object of values to a Map of objects
                Object[] propIds = values.getIds();
                Map<String, Object> propValues = new HashMap<>(propIds.length);
                for (Object propId : propIds) {
                    // work on each key in turn
                    // we are only interested in keys that indicate a list of values
                    if (propId instanceof String) {
                        // get the value out for the specified key
                        Object val = values.get((String) propId, values);
                        // recursively call this method to convert the value
                        propValues.put((String) propId, convertValueForJava(val));
                    }
                }
                value = propValues;
            }
        } else if (value.getClass().isArray()) {
            // convert back a list of Java values
            int length = Array.getLength(value);
            ArrayList<Object> list = new ArrayList<Object>(length);
            for (int i = 0; i < length; i++) {
                list.add(convertValueForJava(Array.get(value, i)));
            }
            value = list;
        } else if (value instanceof CharSequence) {
            // Rhino has some interesting internal classes such as ConsString which cannot be cast to String
            // but fortunately are instanceof CharSequence so we can toString() them.
            value = value.toString();
        }
        return value;
    }

    /**
     * Look at the id's of a native array and try to determine whether it's actually an Array or a Hashmap
     *
     * @param ids id's of the native array
     * @return boolean  true if it's an array, false otherwise (ie it's a map)
     */
    private static boolean isArray(Object[] ids) {
        boolean result = true;
        for (Object id : ids) {
            if (!(id instanceof Integer)) {
                result = false;
                break;
            }
        }
        return result;
    }
}
