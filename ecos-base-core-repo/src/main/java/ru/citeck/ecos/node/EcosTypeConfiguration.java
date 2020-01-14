package ru.citeck.ecos.node;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.records2.RecordRef;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;

@Configuration
public class EcosTypeConfiguration {

    @Autowired
    private EcosTypeService ecosTypeService;

    @PostConstruct
    public void init() {
        ecosTypeService.register(ContentModel.TYPE_CMOBJECT, this::evalDefaultEcosType);
    }

    private RecordRef evalDefaultEcosType(AlfNodeInfo info) {

        Map<QName, Serializable> props = info.getProperties();

        NodeRef type = (NodeRef) props.get(ClassificationModel.PROP_DOCUMENT_TYPE);

        if (type == null) {
            log.warn("Type property of nodeRef is null");
            return null;
        }

        String ecosType = type.getId();

        NodeRef kind = (NodeRef) props.get(ClassificationModel.PROP_DOCUMENT_KIND);
        if (kind != null) {
            ecosType = ecosType + "/" + kind.getId();
        }

        return RecordRef.create("emodel", "type", ecosType);
    }
}
