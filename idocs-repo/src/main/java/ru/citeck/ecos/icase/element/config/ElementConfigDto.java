package ru.citeck.ecos.icase.element.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

@EqualsAndHashCode(of = "nodeRef")
public class ElementConfigDto {

    @Getter private final NodeRef nodeRef;

    @Getter @Setter private String name;
    @Getter @Setter private QName type;
    @Getter @Setter private QName caseClass;
    @Getter @Setter private QName elementType;
    @Getter @Setter private String folderName;
    @Getter @Setter private QName folderType;
    @Getter @Setter private QName folderAssocName;

    @Getter @Setter private QName subcaseType = ContentModel.TYPE_FOLDER;
    @Getter @Setter private QName subcaseAssoc = ContentModel.ASSOC_CONTAINS;

    @Getter private boolean copyElements;
    @Getter private boolean showForAdminOnly;
    @Getter private boolean createSubcase;
    @Getter private boolean removeSubcase;
    @Getter private boolean removeEmptySubcase = true;

    public ElementConfigDto(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public void setCopyElements(Boolean value) {
        if (value != null) {
            copyElements = value;
        }
    }

    public void setShowForAdminOnly(Boolean value) {
        if (value != null) {
            showForAdminOnly = value;
        }
    }

    public void setCreateSubcase(Boolean value) {
        if (value != null) {
            createSubcase = value;
        }
    }

    public void setRemoveSubcase(Boolean value) {
        if (value != null) {
            removeSubcase = value;
        }
    }
    public void setRemoveEmptySubcase(Boolean value) {
        if (value != null) {
            removeEmptySubcase = value;
        }
    }

}
