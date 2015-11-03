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
package ru.citeck.ecos.workflow.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.VariableScope;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.authority.script.ScriptUser;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptableObject;

import ru.citeck.ecos.utils.JavaScriptImplUtils;

public class ActivitiVariableScopeMap implements Map<String, Object> {

    private VariableScope impl;
    private ServiceRegistry services;
    
    public ActivitiVariableScopeMap(VariableScope impl, ServiceRegistry services) {
        this.impl = impl;
        this.services = services;
    }
    
    @Override
    public void clear() {
        impl.removeVariables();
    }

    @Override
    public boolean containsKey(Object key) {
        if(key instanceof String) {
            return impl.hasVariable((String) key);
        } else {
            return false;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return impl.getVariables().entrySet();
    }

    @Override
    public Object get(Object key) {
        if(key instanceof String) {
            return impl.getVariable((String) key);
        } else {
            return false;
        }
    }

    @Override
    public boolean isEmpty() {
        return !impl.hasVariables();
    }

    @Override
    public Set<String> keySet() {
        return impl.getVariableNames();
    }

    @Override
    public Object put(String key, Object value) {
        Object previousValue = get(key);
        if(value instanceof ScriptableObject) {
            ScriptableObject scriptable = (ScriptableObject) value;
            Map<String, Serializable> values = JavaScriptImplUtils.convertScriptableToSerializableMap(scriptable);
            value = values;
        }
        if(value instanceof NativeJavaObject) {
            NativeJavaObject wrapper = (NativeJavaObject) value;
            value = wrapper.unwrap();
        }
        if(value instanceof ScriptNode) {
            ScriptNode node = (ScriptNode) value;
            value = new ActivitiScriptNode(node.getNodeRef(), services);
        }
        if(value instanceof ScriptUser) {
            ScriptUser user = (ScriptUser) value;
            value = new ActivitiScriptNode(user.getPersonNodeRef(), services);
        }
        if(value instanceof ScriptGroup) {
            ScriptGroup group = (ScriptGroup) value;
            value = new ActivitiScriptNode(group.getGroupNodeRef(), services);
        }
        impl.setVariable(key, value);
        return previousValue;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void putAll(Map m) {
        impl.setVariables(m);
    }

    @Override
    public Object remove(Object key) {
        if(key instanceof String) {
            Object previousValue = get(key);
            impl.removeVariable((String) key);
            return previousValue;
        } else {
            return null;
        }
    }

    @Override
    public int size() {
        return impl.getVariables().size();
    }

    @Override
    public Collection<Object> values() {
        return impl.getVariables().values();
    }

}
