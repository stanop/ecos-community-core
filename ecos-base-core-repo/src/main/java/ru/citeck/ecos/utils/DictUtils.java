package ru.citeck.ecos.utils;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DictUtils {

    public static QName QNAME = QName.createQName("", "dictUtils");

    private static String TXN_CONSTRAINTS_CACHE = DictUtils.class.getName();

    private DictionaryService dictionaryService;
    private MessageService messageService;

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

    public String getPropertyDisplayName(QName name, String value) {

        Map<String, String> mapping = getPropertyDisplayNameMapping(name);
        return mapping != null && mapping.containsKey(value) ? mapping.get(value) : value;
    }

    public Map<String, String> getPropertyDisplayNameMapping(QName name) {

        Map<QName, Map<String, String>> cache = TransactionalResourceHelper.getMap(TXN_CONSTRAINTS_CACHE);

        return cache.computeIfAbsent(name, n -> {

            ListOfValuesConstraint constraint = getListOfValuesConstraint(name);
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

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        dictionaryService = serviceRegistry.getDictionaryService();
        messageService = serviceRegistry.getMessageService();
    }
}
