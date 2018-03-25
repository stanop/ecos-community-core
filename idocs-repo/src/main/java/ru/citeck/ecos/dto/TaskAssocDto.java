package ru.citeck.ecos.dto;

/**
 * Task assoc data transfer object
 */
public class TaskAssocDto {

    /**
     * Assoc type
     */
    private String assocType;

    /**
     * Node reference
     */
    private String nodeRef;

    /** Getters and setters */

    public String getAssocType() {
        return assocType;
    }

    public void setAssocType(String assocType) {
        this.assocType = assocType;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }
}
