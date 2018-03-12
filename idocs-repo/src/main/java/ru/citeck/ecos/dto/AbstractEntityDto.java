package ru.citeck.ecos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Abstract entity dto
 */
public class AbstractEntityDto implements Serializable {

    /**
     * Node uuid
     */
    private String nodeUUID;

    /**
     * Created datetime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created;

    /**
     * Creator
     */
    private String creator;

    /**
     * Modified datetime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modified;

    /**
     * Modifier
     */
    private String modifier;

    /**
     * Title
     */
    private String title;

    /**
     * Description
     */
    private String description;

    /** Getters and setters */

    public String getNodeUUID() {
        return nodeUUID;
    }

    public void setNodeUUID(String nodeUUID) {
        this.nodeUUID = nodeUUID;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
