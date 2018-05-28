package ru.citeck.ecos.icase.element.config;

import lombok.Getter;
import lombok.Setter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class CategoryConfigDto extends ElementConfigDto {

    @Getter @Setter private QName categoryProperty;

    public CategoryConfigDto(NodeRef nodeRef) {
        super(nodeRef);
    }
}
