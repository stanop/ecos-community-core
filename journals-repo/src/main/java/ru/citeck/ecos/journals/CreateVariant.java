package ru.citeck.ecos.journals;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.Map;
import java.util.Objects;

public class CreateVariant {

    private MLText title;
    private NodeRef nodeRef;
    private NodeRef destination;

    @Getter @Setter private String type;
    @Getter @Setter private String formId;
    @Getter @Setter private boolean isDefault;
    @Getter @Setter private String createArguments;
    @Getter @Setter private String recordRef;
    @Getter @Setter private String formKey;
    @Getter @Setter private Map<String, String> attributes;

    public String getNodeRef() {
        return nodeRef != null ? nodeRef.toString() : null;
    }

    @JsonIgnore
    public NodeRef getNode() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getDestination() {
        return destination != null ? destination.toString() : null;
    }

    @JsonIgnore
    public NodeRef getDestinationRef() {
        return destination;
    }

    public void setDestination(NodeRef destination) {
        this.destination = destination;
    }

    public void setTitle(String title) {
        MLText mlText = new MLText();
        mlText.put(I18NUtil.getLocale(), title);
        this.title = mlText;
    }

    @JsonIgnore
    public void setTitle(MLText title) {
        this.title = title;
    }

    @JsonIgnore
    public MLText getMLTitle() {
        return title;
    }

    @JsonProperty("title")
    public String getTitle() {
        if (title != null) {
            return title.getClosestValue(I18NUtil.getLocale());
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateVariant that = (CreateVariant) o;
        return isDefault == that.isDefault &&
                Objects.equals(nodeRef, that.nodeRef) &&
                Objects.equals(title, that.title) &&
                Objects.equals(destination, that.destination) &&
                Objects.equals(type, that.type) &&
                Objects.equals(formId, that.formId) &&
                Objects.equals(createArguments, that.createArguments) &&
                Objects.equals(recordRef, that.recordRef) &&
                Objects.equals(formKey, that.formKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeRef, title, destination, type, formId, isDefault, createArguments, recordRef, formKey);
    }
}
