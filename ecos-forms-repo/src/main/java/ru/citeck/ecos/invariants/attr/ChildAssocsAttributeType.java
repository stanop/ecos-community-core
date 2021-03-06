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

import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.invariants.AbstractInvariantAttributeType;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantPriority;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.invariants.InvariantScope.AttributeScopeKind;
import ru.citeck.ecos.model.AttributeModel;

public class ChildAssocsAttributeType extends AbstractInvariantAttributeType {
    
    private static final Log logger = LogFactory.getLog(ChildAssocsAttributeType.class);

    @Override
    public QName getSupportedAttributeType() {
        return AttributeModel.TYPE_CHILD_ASSOCIATION;
    }

    @Override
    public InvariantScope getAttributeScope(QName attributeName) {
        return new InvariantScope(attributeName, AttributeScopeKind.CHILD_ASSOCIATION);
    }

    @Override
    public InvariantScope getAttributeTypeScope(QName attributeSubtype) {
        return new InvariantScope(attributeSubtype, AttributeScopeKind.CHILD_ASSOCIATION_TYPE);
    }

    @Override
    public List<InvariantDefinition> getDefaultInvariants(QName attributeName, List<ClassDefinition> classes) {
        AssociationDefinition assocDef = dictionaryService.getAssociation(attributeName);
        
        InvariantDefinition.Builder builder = new InvariantDefinition.Builder(prefixResolver);
        builder.pushScope(assocDef)
               .pushScope(assocDef.getSourceClass())
               .priority(InvariantPriority.COMMON);
        
        return TargetAssocsAttributeType.getDefaultInvariants(assocDef, prefixResolver, messageLookup);
    }

}
