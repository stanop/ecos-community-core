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
package ru.citeck.ecos.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

public class ExceptQNamePattern implements QNamePattern {
    
    private final Set<QName> restrictedQNames;
    
    public ExceptQNamePattern(QName ...restricted) {
        this.restrictedQNames = 
                restricted.length == 0 ? Collections.<QName>emptySet() :
                restricted.length == 1 ? Collections.singleton(restricted[0]) :
                new HashSet<>(Arrays.asList(restricted));
    }
    
    @Override
    public boolean isMatch(QName qname) {
        if(qname == null) return false;
        return !restrictedQNames.contains(qname);
    }
    
}
