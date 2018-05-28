package ru.citeck.ecos.icase.element.config;

import lombok.Getter;
import lombok.Setter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class AssocsConfigDto extends ElementConfigDto {

    @Getter @Setter private QName assocName;
    @Getter @Setter private String assocType;

    public AssocsConfigDto(NodeRef nodeRef) {
        super(nodeRef);
    }
}
