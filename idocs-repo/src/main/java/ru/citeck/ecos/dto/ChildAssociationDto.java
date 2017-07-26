package ru.citeck.ecos.dto;

import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.List;

/**
 * Child association data transfer object
 */
public class ChildAssociationDto implements Serializable {

    /**
     * Properties
     */
    private List<AbstractMap.SimpleEntry<QName, Serializable>> properties;

    /**
     * Child associations
     */
    private List<AbstractMap.SimpleEntry<QName, List<AbstractMap.SimpleEntry<QName, Serializable>>>> childAssociations;


    public List<AbstractMap.SimpleEntry<QName, Serializable>> getProperties() {
        return properties;
    }

    public void setProperties(List<AbstractMap.SimpleEntry<QName, Serializable>> properties) {
        this.properties = properties;
    }

    public List<AbstractMap.SimpleEntry<QName, List<AbstractMap.SimpleEntry<QName, Serializable>>>> getChildAssociations() {
        return childAssociations;
    }

    public void setChildAssociations(List<AbstractMap.SimpleEntry<QName, List<AbstractMap.SimpleEntry<QName, Serializable>>>> childAssociations) {
        this.childAssociations = childAssociations;
    }
}
