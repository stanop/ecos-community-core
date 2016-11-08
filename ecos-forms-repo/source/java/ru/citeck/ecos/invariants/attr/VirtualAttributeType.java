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
package ru.citeck.ecos.invariants.attr;

import java.util.*;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.invariants.AbstractInvariantAttributeType;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantPriority;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.invariants.InvariantScope.AttributeScopeKind;
import ru.citeck.ecos.model.AttributeModel;

public class VirtualAttributeType extends AbstractInvariantAttributeType {

    @Override
    public QName getSupportedAttributeType() {
        return AttributeModel.TYPE_VIRTUAL;
    }

    @Override
    public InvariantScope getAttributeScope(QName attributeName) {
        return new InvariantScope(attributeName, AttributeScopeKind.PROPERTY);
    }

    @Override
    public InvariantScope getAttributeTypeScope(QName attributeSubtype) {
        return null;
    }

    @Override
    public List<InvariantDefinition> getDefaultInvariants(QName attributeName, List<ClassDefinition> classes) {
        PropertyDefinition propDef = dictionaryService.getProperty(attributeName);
        InvariantDefinition.Builder builder = new InvariantDefinition.Builder(prefixResolver)
                .pushScope(propDef)
                .priority(InvariantPriority.COMMON);
        List<InvariantDefinition> invariants = PropertiesAttributeType.getDefaultInvariants(propDef, builder, messageLookup);

        if (attributeName.equals(AttributeModel.ATTR_TYPES)) {
            List<String> typeNames = new LinkedList<>();
            for(ClassDefinition classDef : classes) {
                if (!classDef.isAspect())
                    typeNames.add(classDef.getName().toPrefixString(prefixResolver));
            }
            Collections.reverse(typeNames);
            invariants.add(builder.feature(Feature.DEFAULT).explicit(typeNames).build());
        } else if(attributeName.equals(AttributeModel.ATTR_ASPECTS)) {
            Set<String> aspectNames = new HashSet<>();
            for(ClassDefinition classDef : classes) {
                if (classDef.isAspect()) {
                    aspectNames.add(classDef.getName().toPrefixString(prefixResolver));
                }
                List<AspectDefinition> defaultAspects = classDef.getDefaultAspects();
                for (AspectDefinition def : defaultAspects) {
                    aspectNames.add(def.getName().toPrefixString(prefixResolver));
                }
            }
            invariants.add(builder.feature(Feature.DEFAULT).explicit(aspectNames).build());
        }
        
        return invariants;
    }

}
