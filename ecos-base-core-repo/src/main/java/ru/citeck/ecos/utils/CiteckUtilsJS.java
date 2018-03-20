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

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptableQNameMap;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CiteckUtilsJS extends AlfrescoScopableProcessorExtension implements NamespacePrefixResolverProvider {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private ValueConverter converter = new ValueConverter();
    private NamespacePrefixResolver prefixResolver;

    public QName createQName(String s) {
        QName qname;
        if (s.indexOf(QName.NAMESPACE_BEGIN) != -1) {
            qname = QName.createQName(s);
        } else {
            qname = QName.createQName(s, prefixResolver);
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

        @SuppressWarnings("unchecked")
        Map<String, Object> values = (Map<String, Object>) value;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result.put(QName.resolveToQName(prefixResolver, entry.getKey()), entry.getValue());
        }

        return result;
    }

    public ScriptableQNameMap<String, Object> toScriptQNameMap(Map<QName, Object> map) {

        ScriptableQNameMap<String, Object> result = new ScriptableQNameMap<>(this);

        for (QName qname : map.keySet()) {
            Object value = map.get(qname);
            result.put(qname, value);
        }

        return result;
    }

    public void setMLText(ScriptNode node, String property, Serializable valueObj) {

        @SuppressWarnings("unchecked")
        Map<String, String> value = (Map) converter.convertValueForRepo(valueObj);

        MLText mlText = new MLText();
        for (Map.Entry<String, String> entry : value.entrySet()) {
            mlText.put(Locale.forLanguageTag(entry.getKey()), entry.getValue());
        }

        node.getProperties().put(property, mlText);
    }

    public String toUTF8(String str) {
        return new String(str.getBytes(), UTF8);
    }

    @Override
    public NamespacePrefixResolver getNamespacePrefixResolver() {
        return prefixResolver;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }
}
