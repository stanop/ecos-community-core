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
     * Content url
     */
    private String contentUrl;

    /**
     * Node reference
     */
    private String nodeRef;

    /**
     * Parent reference
     */
    private String parentRef;

    /**
     * Properties
     */
    private List<AbstractMap.SimpleEntry<QName, Serializable>> properties;

    /**
     * Child associations
     */
    private List<AbstractMap.SimpleEntry<QName, ChildAssociationDto>> childAssociations;

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getParentRef() {
        return parentRef;
    }

    public void setParentRef(String parentRef) {
        this.parentRef = parentRef;
    }

    public List<AbstractMap.SimpleEntry<QName, Serializable>> getProperties() {
        return properties;
    }

    public void setProperties(List<AbstractMap.SimpleEntry<QName, Serializable>> properties) {
        this.properties = properties;
    }

    public List<AbstractMap.SimpleEntry<QName, ChildAssociationDto>> getChildAssociations() {
        return childAssociations;
    }

    public void setChildAssociations(List<AbstractMap.SimpleEntry<QName, ChildAssociationDto>> childAssociations) {
        this.childAssociations = childAssociations;
    }
}
