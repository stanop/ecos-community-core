package ru.citeck.ecos.utils;

import ecos.com.google.common.cache.Cache;
import ecos.com.google.common.cache.CacheBuilder;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class DictUtils {

    public static final QName QNAME = QName.createQName("", "dictUtils");

    private static String TXN_CONSTRAINTS_CACHE = DictUtils.class.getName();
    private static int CACHE_AGE_SECONDS = 600;

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private MessageService messageService;

    private Cache<QName, Map<String, String>> cachedDisplayNameMapping;

    /**
     * Search property definition in specified container or associated default aspects
     * @param containerName aspect or type name. If null then default property definition will be returned
     * @param propertyName property name. Must be not null
     * @return property definition or null if definition not found
     * @throws NullPointerException if propertyName is null
     */
    public PropertyDefinition getPropDef(QName containerName, QName propertyName) {

        PropertyDefinition propDef = dictionaryService.getProperty(propertyName);

        if (propDef == null) {
            return null;
        }

        ClassDefinition containerClass = null;
        if (containerName != null) {
            containerClass = dictionaryService.getClass(containerName);
        }

        if (containerClass != null) {

            ClassDefinition propContainerClass = propDef.getContainerClass();

            if (!propContainerClass.equals(containerClass)) {

                if (dictionaryService.isSubClass(containerClass.getName(), propContainerClass.getName())) {

                    propContainerClass = containerClass;

                } else if (propContainerClass.isAspect()) {

                    for (ClassDefinition aspectDef : containerClass.getDefaultAspects(true)) {
                        if (dictionaryService.isSubClass(aspectDef.getName(), propContainerClass.getName())) {
                            propContainerClass = aspectDef;
                        }
                    }
                }

                propDef = dictionaryService.getProperty(propContainerClass.getName(), propertyName);
            }
        }

        return propDef;
    }

    public String getTypeTitle(QName typeName) {
        TypeDefinition type = dictionaryService.getType(typeName);
        return type.getTitle(messageService);
    }

    public ClassAttributeDefinition getAttDefinition(String name) {

        if (StringUtils.isBlank(name)) {
            return null;
        }

        QName field = QName.resolveToQName(namespaceService, name);
        if (field == null) {
            return null;
        }

        AssociationDefinition assocDef = dictionaryService.getAssociation(field);
        if (assocDef != null) {
            return assocDef;
        }
        return dictionaryService.getProperty(field);
    }

    public String getPropertyDisplayName(QName name, String value) {
        return getPropertyDisplayName(null, name, value);
    }

    public String getPropertyDisplayName(QName scope, QName name, String value) {
        Map<String, String> mapping = getPropertyDisplayNameMapping(scope, name);
        return mapping != null && mapping.containsKey(value) ? mapping.get(value) : value;
    }

    public Map<String, String> getPropertyDisplayNameMapping(QName name) {
        return getPropertyDisplayNameMapping(null, name);
    }

    public Map<String, String> getPropertyDisplayNameMapping(QName scope, QName name) {

        Map<Pair<QName, QName>, Map<String, String>> cache = TransactionalResourceHelper.getMap(TXN_CONSTRAINTS_CACHE);
        Pair<QName, QName> key = new Pair<>(scope, name);

        return cache.computeIfAbsent(key, n -> {

            PropertyDefinition propDef = getPropDef(scope, name);

            ListOfValuesConstraint constraint = getListOfValuesConstraint(propDef);
            if (constraint == null) {
                return null;
            }

            Map<String, String> result = new HashMap<>();

            for (String value : constraint.getAllowedValues()) {

                String display = constraint.getDisplayLabel(value, messageService);
                result.put(value, display);
            }

            return result;
        });
    }

    public Map<String, String> getPropertyDisplayNameMappingWithChildren(QName parent, QName field) {

        Cache<QName, Map<String, String>> cache = this.getCachedMapping();

        return cache.asMap().computeIfAbsent(parent, n -> {

            Map<String, String> result = new HashMap<>();

            Map<String, String> parentMapping = this.getPropertyDisplayNameMapping(parent, field);
            if (parentMapping != null) {
                result.putAll(parentMapping);
            }

            Collection<QName> children = this.getChildClassNames(parent, false);
            for (QName child : children) {
                Map<String, String> childMapping = this.getPropertyDisplayNameMapping(child, field);
                if (childMapping != null) {
                    result.putAll(childMapping);
                }
            }

            return result;
        });
    }

    /**
     * Returns a list of constraints for the specified property
     * @param propertyName property name. Must be not null
     * @return list of values constraint or null
     * @throws NullPointerException if propertyName is null
     */
    public ListOfValuesConstraint getListOfValuesConstraint(QName propertyName) {

        return getListOfValuesConstraint(dictionaryService.getProperty(propertyName));
    }

    public ListOfValuesConstraint getListOfValuesConstraint(PropertyDefinition propDef) {

        if (propDef != null) {
            List<ConstraintDefinition> constraintDefinitions = propDef.getConstraints();

            for (ConstraintDefinition constraintDefinition : constraintDefinitions) {
                Constraint constraint = constraintDefinition.getConstraint();

                if (constraint instanceof ListOfValuesConstraint) {
                    return (ListOfValuesConstraint) constraint;
                }
            }
        }

        return null;
    }

    public Cache<QName, Map<String, String>> getCachedMapping() {
        if (cachedDisplayNameMapping == null) {
            synchronized (DictUtils.class) {
                if (cachedDisplayNameMapping == null) {
                    this.cachedDisplayNameMapping = CacheBuilder.newBuilder()
                            .expireAfterWrite(CACHE_AGE_SECONDS, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return this.cachedDisplayNameMapping;
    }

    public Collection<QName> getChildClassNames(QName className, boolean recursive) {
        ClassDefinition classDef = dictionaryService.getClass(className);
        if(classDef == null) {
            throw new IllegalArgumentException("Class is not registered: " + className);
        } else if(classDef.isAspect()) {
            return dictionaryService.getSubAspects(className, recursive);
        } else {
            return dictionaryService.getSubTypes(className, recursive);
        }
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        dictionaryService = serviceRegistry.getDictionaryService();
        messageService = serviceRegistry.getMessageService();
        namespaceService = serviceRegistry.getNamespaceService();
    }
}
