package ru.citeck.ecos.icase.element.config;

import lombok.Getter;
import lombok.Setter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class KeyPropConfig extends ElementConfigDto {

    @Getter @Setter private QName elementKey;
    @Getter @Setter protected QName caseKey;

    public KeyPropConfig(NodeRef nodeRef) {
        super(nodeRef);
    }
}
