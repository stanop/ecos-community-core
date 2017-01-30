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

import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CiteckUtilsJS extends AlfrescoScopableProcessorExtension {

    private ValueConverter converter = new ValueConverter();

    public QName createQName(String s) {
        QName qname;
        if (s.indexOf(QName.NAMESPACE_BEGIN) != -1) {
            qname = QName.createQName(s);
        } else {
            qname = QName.createQName(s, this.serviceRegistry.getNamespaceService());
        }
        return qname;
    }

    public Map<QName, Object> toQNameMap(Serializable map) {
        if (map == null) {
            return Collections.emptyMap();
        }

        Serializable value = converter.convertValueForRepo(map);
        Map<QName, Object> result = new HashMap<>();

        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("parameter 'map' has incorrect type: " + value.getClass().getName()
                                                                      + " expected: " + Map.class.getName());
        }

        NamespaceService namespaceService = this.serviceRegistry.getNamespaceService();
        Map<String, Object> values = (Map<String, Object>)value;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result.put(QName.resolveToQName(namespaceService, entry.getKey()), entry.getValue());
        }

        return result;
    }

}
