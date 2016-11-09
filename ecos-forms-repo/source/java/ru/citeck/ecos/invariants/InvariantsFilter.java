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
package ru.citeck.ecos.invariants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.batik.svggen.font.table.ClassDef;
import org.apache.commons.lang.builder.CompareToBuilder;

import ru.citeck.ecos.attr.NodeAttributeService;
import ru.citeck.ecos.invariants.InvariantScope.AttributeScopeKind;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.utils.DictionaryUtils;

class InvariantsFilter {
    
    // according to CompareToBuilder.append javadocs, null is less than any other object
    private static QName FIRST_NAME = null;
    
    // '~' == '\u007E' is almost the biggest ASCII character 
    // and should be greater than any other characters, used in qname
    private static QName LAST_NAME = QName.createQName("~", "~");
    
    private DictionaryService dictionaryService;
    private NodeAttributeService nodeAttributeService;
    
    private Map<QName, InvariantAttributeType> attributeTypes;
    
    private NavigableMap<InvariantScope, List<InvariantDefinition>> invariantsByClass;
    private NavigableMap<InvariantScope, List<InvariantDefinition>> invariantsByAttribute;
    private Map<String, List<InvariantDefinition>> invariantsBySource;
    
    // index by class: class, attribute, attribute kind
    private static Comparator<InvariantScope> classFirstComparator = new Comparator<InvariantScope>() {
        @Override
        public int compare(InvariantScope scope1, InvariantScope scope2) {
            return new CompareToBuilder()
                    .append(scope1.getClassScope(), scope2.getClassScope())
                    .append(scope1.getAttributeScope(), scope2.getAttributeScope())
                    .append(scope1.getAttributeScopeKind(), scope2.getAttributeScopeKind())
                    .toComparison();
        }
    };
    
    // index by attribute: attribute kind, attribute, class
    private static Comparator<InvariantScope> attributeFirstComparator = new Comparator<InvariantScope>() {
        @Override
        public int compare(InvariantScope scope1, InvariantScope scope2) {
            return new CompareToBuilder()
                    .append(scope1.getAttributeScopeKind(), scope2.getAttributeScopeKind())
                    .append(scope1.getAttributeScope(), scope2.getAttributeScope())
                    .append(scope1.getClassScope(), scope2.getClassScope())
                    .toComparison();
        }
    };
    
    public InvariantsFilter() {
        this.invariantsByClass = new TreeMap<>(classFirstComparator);
        this.invariantsByAttribute = new TreeMap<>(attributeFirstComparator);
        this.invariantsBySource = new HashMap<>();
    }
    
    public void registerInvariants(Collection<InvariantDefinition> invariants, String sourceId) {
        if(invariantsBySource.containsKey(sourceId)) {
            unregisterInvariants(sourceId);
        }
        
        List<InvariantDefinition> invariantsCopy = new ArrayList<>(invariants);
        invariantsBySource.put(sourceId, invariantsCopy);
        
        for(InvariantDefinition invariant : invariants) {
            registerInvariant(invariant, invariantsByClass);
            registerInvariant(invariant, invariantsByAttribute);
        }
    }
    
    public void unregisterInvariants(Collection<InvariantDefinition> invariants) {
        for(InvariantDefinition invariant : invariants) {
            unregisterInvariant(invariant, invariantsByClass);
            unregisterInvariant(invariant, invariantsByAttribute);
        }
    }
    
    public void unregisterInvariants(String sourceId) {
        List<InvariantDefinition> invariants = invariantsBySource.get(sourceId);
        if(invariants == null) return;
        invariantsBySource.remove(sourceId);
        unregisterInvariants(invariants);
    }
    
    private static void registerInvariant(InvariantDefinition invariant, Map<InvariantScope, List<InvariantDefinition>> invariants) {
        InvariantScope scope = invariant.getScope();
        List<InvariantDefinition> scopedInvariants = invariants.get(scope);
        if(scopedInvariants == null) {
            scopedInvariants = new LinkedList<>();
            invariants.put(scope, scopedInvariants);
        }
        scopedInvariants.add(invariant);
    }
    
    private static void unregisterInvariant(InvariantDefinition invariant, Map<InvariantScope, List<InvariantDefinition>> invariants) {
        InvariantScope scope = invariant.getScope();
        List<InvariantDefinition> scopedInvariants = invariants.get(scope);
        if(scopedInvariants == null) return;
        scopedInvariants.remove(invariant);
    }
    
    public List<InvariantDefinition> getInvariants(String sourceId) {
        List<InvariantDefinition> invariants = invariantsBySource.get(sourceId);
        return invariants != null ? new ArrayList<>(invariants) : null;
    }
    
    public List<InvariantDefinition> searchMatchingInvariants(Collection<QName> classNames) {
        return this.searchMatchingInvariants(classNames, null, true);
    }
    
    /**
     * Search invariants, matching specified classes and attributes.
     * If attributeNames is null, then invariants for all attributes defined in specified classes returns
     * The invariants list is ordered by priority - the highest priority first.
     * 
     * @param classNames classes to search invariants
     * @param addDefault set true to add default invariants to list, false - only custom invariants
     * @return ordered list of invariants
     */
    public List<InvariantDefinition> searchMatchingInvariants(Collection<QName> classNames, Collection<QName> attributeNames, boolean addDefault) {

        Set<QName> attributes;
        if (attributeNames != null) {
            attributes = new HashSet<>(attributeNames);
            attributes.add(AttributeModel.ATTR_NODEREF);
            attributes.add(AttributeModel.ATTR_ASPECTS);
            attributes.add(AttributeModel.ATTR_PARENT);
            attributes.add(AttributeModel.ATTR_PARENT_ASSOC);
            attributes.add(AttributeModel.ATTR_TYPES);
        } else {
            attributes = getDefinedAttributeNames(classNames);
        }

        List<ClassDefinition> allInvolvedClasses = DictionaryUtils.getClasses(classNames, dictionaryService);
        List<ClassDefinition> classes = new ArrayList<>(getDefiningClassNames(allInvolvedClasses, attributes));
        for (ClassDefinition clazz : classes) {
            if (!allInvolvedClasses.contains(clazz)) {
                allInvolvedClasses.add(clazz);
            }
        }

        Set<InvariantDefinition> invariants = new LinkedHashSet<>();
        
        // search by class
        for(ClassDefinition classDef : allInvolvedClasses) {
            Collection<List<InvariantDefinition>> invariantsByClass = this.invariantsByClass.subMap(
                                            new InvariantScope(classDef.getName(), FIRST_NAME, null),
                                            new InvariantScope(classDef.getName(), LAST_NAME, null)).values();
            invariants.addAll(filterByAttributes(invariantsByClass, attributes));
        }

        Set<Pair<QName,QName>> addedAttributeTypes = new HashSet<>(attributes.size());
        
        for(QName attributeName : attributes) {
            QName attributeTypeName = nodeAttributeService.getAttributeType(attributeName);

            InvariantAttributeType attributeType = attributeTypes.get(attributeTypeName);
            if(attributeType == null) continue; // unsupported

            addInvariants(
                    invariantsByAttribute.get(attributeType.getAttributeScope(attributeName)),
                    invariants);
            if(addDefault) {
                addInvariants(
                        attributeType.getDefaultInvariants(attributeName, allInvolvedClasses),
                        invariants);
            }

            QName attributeSubtype = nodeAttributeService.getAttributeSubtype(attributeName);
            if(!addedAttributeTypes.contains(new Pair<>(attributeTypeName, attributeSubtype))) {
                InvariantScope attributeTypeScope = attributeType.getAttributeTypeScope(attributeSubtype);
                if(attributeTypeScope != null) {
                    addInvariants(
                            invariantsByAttribute.get(attributeTypeScope),
                            invariants);
                }
                addedAttributeTypes.add(new Pair<>(attributeTypeName, attributeSubtype));
            }
        }

        // search by pure attributes
        for(AttributeScopeKind scopeKind : AttributeScopeKind.values()) {
            if(!scopeKind.isConcrete()) {
                addInvariants(invariantsByAttribute.get(new InvariantScope(scopeKind)), invariants);
            }
        }
        
        // ordering
        return orderInvariants(allInvolvedClasses, invariants);
    }

    private Set<ClassDefinition> getDefiningClassNames(Collection<ClassDefinition> containers, Collection<QName> attributes) {
        List<ClassDefinition> primaryContainers = new ArrayList<>(containers);
        for (ClassDefinition container : containers) {
            primaryContainers.addAll(container.getDefaultAspects(false));
        }
        Set<ClassDefinition> definingClassNames = new HashSet<>();
        for (QName attribute : attributes) {
            PropertyDefinition propDef = dictionaryService.getProperty(attribute);
            if (propDef != null) {
                PropertyDefinition definition = null;
                for (ClassDefinition container : primaryContainers) {
                    definition = dictionaryService.getProperty(container.getName(), attribute);
                    if (definition != null) {
                        definingClassNames.add(container);
                        break;
                    }
                }
                if (definition == null) {
                    definingClassNames.add(propDef.getContainerClass());
                }
                continue;
            }
            AssociationDefinition assocDef = dictionaryService.getAssociation(attribute);
            if (assocDef != null) {
                definingClassNames.add(assocDef.getSourceClass());
            }
        }
        return definingClassNames;
    }

    private List<InvariantDefinition> filterByAttributes(Collection<List<InvariantDefinition>> source, Collection<QName> attributes) {
        List<InvariantDefinition> result = new ArrayList<>();
        List<Pair<QName,QName>> attributesWithSubType = new ArrayList<>(attributes.size());
        for (QName attribute : attributes) {
            attributesWithSubType.add(new Pair<>(attribute, nodeAttributeService.getAttributeSubtype(attribute)));
        }
        for (List<InvariantDefinition> list : source) {
            if (list == null) continue;
            for (InvariantDefinition def : list) {
                if (def == null) continue;
                InvariantScope scope = def.getScope();
                for (Pair<QName, QName> attribute : attributesWithSubType) {

                    QName attributeScope;
                    switch (scope.getAttributeScopeKind()) {
                        case ASSOCIATION:
                        case PROPERTY:
                        case CHILD_ASSOCIATION:
                            attributeScope = attribute.getFirst();
                            break;
                        case ASSOCIATION_TYPE:
                        case PROPERTY_TYPE:
                        case CHILD_ASSOCIATION_TYPE:
                            attributeScope = attribute.getSecond();
                            break;
                        default:
                            attributeScope = attribute.getFirst();
                    }
                    if (attributeScope.equals(scope.getAttributeScope())) {
                        result.add(def);
                    }
                }
            }
        }
        return result;
    }
    
    public List<InvariantDefinition> orderInvariants(List<ClassDefinition> allInvolvedClasses,
            Collection<InvariantDefinition> invariants) {
        List<InvariantDefinition> orderedInvariants = new ArrayList<>(invariants);
        Collections.sort(orderedInvariants, new OrderingComparator(allInvolvedClasses));
        return orderedInvariants;
    }
    
    private static void addInvariants(List<InvariantDefinition> source, Set<InvariantDefinition> target) {
        if(source != null) {
            target.addAll(source);
        }
    }
    
    private static void addInvariants(Collection<List<InvariantDefinition>> source, Set<InvariantDefinition> target) {
        for(List<InvariantDefinition> list : source) {
            addInvariants(list, target);
        }
    }
    
    private Set<QName> getDefinedAttributeNames(Collection<QName> classNames) {
        Set<QName> attributeNames = new HashSet<>(50);
        for(QName className : classNames) {
            attributeNames.addAll(nodeAttributeService.getDefinedAttributeNames(className, false));
        }
        return attributeNames;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeAttributeService(NodeAttributeService nodeAttributeService) {
        this.nodeAttributeService = nodeAttributeService;
    }

    public void setAttributeTypesRegistry(Map<QName, InvariantAttributeType> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    // "highest priority first" comparator
    private static class OrderingComparator implements Comparator<InvariantDefinition> {
        
        private Map<QName, Integer> classPriorities;
        
        public OrderingComparator(List<ClassDefinition> classes) {
            classPriorities = new HashMap<>(classes.size());
            int priority = 0;
            for(ClassDefinition classDef : classes) {
                classPriorities.put(classDef.getName(), priority);
                priority++;
            }
        }
        
        @Override
        public int compare(InvariantDefinition inv1, InvariantDefinition inv2) {
            int result;
            
            // final first
            result = Boolean.compare(inv2.isFinal(), inv1.isFinal());
            if(result != 0) return result;
            
            // highest attribute priority first:
            result = inv1.getAttributeScopeKind().compareTo(inv2.getAttributeScopeKind());
            if(result != 0) return result;
            
            // highest invariant priority first
            result = inv2.getPriority().compareTo(inv1.getPriority());
            if(result != 0) return result;
            
            // highest class priority first
            QName class1 = inv1.getClassScope();
            QName class2 = inv2.getClassScope();
            int priority1 = class1 != null ? classPriorities.get(class1) : -1;
            int priority2 = class2 != null ? classPriorities.get(class2) : -1;
            result = Integer.compare(priority2, priority1);
            if(result != 0) return result;
            
            return 0;
        }
        
    }

}
