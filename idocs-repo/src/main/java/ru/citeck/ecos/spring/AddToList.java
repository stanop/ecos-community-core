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

import java.util.List;

import org.springframework.beans.factory.InitializingBean;

public class AddToList<V> implements InitializingBean {
    private List<V> list;
    private V value;
    private List<V> values;
    private V before;
    
    public void setList(List<V> list) {
        this.list = list;
    }
    public void setValue(V value) {
        this.value = value;
    }
    public void setValues(List<V> values) {
        this.values = values;
    }
    public void setBefore(V before) {
        this.before = before;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        int index = before != null ? list.indexOf(before) : -1;
        if(index >= 0) {
            if(values != null) {
                list.addAll(index, values);
            }
            if(value != null) {
                list.add(index, value);
            }
        } else {
            if(value != null) {
                list.add(value);
            }
            if(values != null) {
                list.addAll(values);
            }
        }
    }
}
