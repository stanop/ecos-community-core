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

import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.invariants.AbstractInvariantAttributeType;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantPriority;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.invariants.InvariantScope.AttributeScopeKind;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.search.FieldType;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.search.SearchPredicate;

public class TargetAssocsAttributeType extends AbstractInvariantAttributeType {
    
    private static final Log logger = LogFactory.getLog(TargetAssocsAttributeType.class);

    @Override
    public QName getSupportedAttributeType() {
        return AttributeModel.TYPE_TARGET_ASSOCIATION;
    }

    @Override
    public InvariantScope getAttributeScope(QName attributeName) {
        return new InvariantScope(attributeName, AttributeScopeKind.ASSOCIATION);
    }

    @Override
    public InvariantScope getAttributeTypeScope(QName attributeSubtype) {
        return new InvariantScope(attributeSubtype, AttributeScopeKind.ASSOCIATION_TYPE);
    }

    @Override
    public List<InvariantDefinition> getDefaultInvariants(QName attributeName, List<ClassDefinition> classes) {
        AssociationDefinition assocDef = dictionaryService.getAssociation(attributeName);
        return getDefaultInvariants(assocDef, prefixResolver, messageLookup);
    }

    public static List<InvariantDefinition> getDefaultInvariants(
            AssociationDefinition assocDef, 
            NamespacePrefixResolver prefixResolver, 
            MessageLookup messageLookup) {
        List<InvariantDefinition> invariants = new LinkedList<>();
        
        InvariantDefinition.Builder builder = new InvariantDefinition.Builder(prefixResolver);
        builder.pushScope(assocDef)
               .pushScope(assocDef.getSourceClass())
               .priority(InvariantPriority.COMMON);
        
        String title = assocDef.getTitle(messageLookup);
        if(title != null) {
            invariants.add(builder.feature(Feature.TITLE).explicit(title).build());
        }
        
        String description = assocDef.getDescription(messageLookup);
        if(description != null) {
            invariants.add(builder.feature(Feature.DESCRIPTION).explicit(description).build());
        }
        
        if(assocDef.isProtected()) {
            invariants.add(builder.feature(Feature.PROTECTED).explicit(true).buildFinal());
        }
        
        if(assocDef.isTargetMandatory()) {
            invariants.add(builder.feature(Feature.MANDATORY).explicit(true).buildFinal());
        }
        
        if(assocDef.isTargetMany()) {
            invariants.add(builder.feature(Feature.MULTIPLE).explicit(true).build());
        } else {
            invariants.add(builder.feature(Feature.MULTIPLE).explicit(false).buildFinal());
        }
        
        SearchCriteria criteria = new SearchCriteria(prefixResolver);
        ClassDefinition targetClass = assocDef.getTargetClass();
        if(targetClass.isAspect()) {
            criteria.addCriteriaTriplet(FieldType.ASPECT, SearchPredicate.ASPECT_EQUALS, targetClass.getName());
        } else {
            criteria.addCriteriaTriplet(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, targetClass.getName());
        }
        invariants.add(builder.feature(Feature.OPTIONS).criteria(criteria).build());
        
        return invariants;
    }

}
