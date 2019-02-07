package ru.citeck.ecos.eform.repo;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.content.dao.NodeDataReader;
import ru.citeck.ecos.eform.model.EcosFormModel;
import ru.citeck.ecos.model.EFormModel;
import ru.citeck.ecos.model.EcosContentModel;

import java.io.Serializable;
import java.util.Map;

public class EcosFormNodeDataReader implements NodeDataReader<EcosFormModel> {

    private NodeService nodeService;

    @Autowired
    public EcosFormNodeDataReader(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public EcosFormModel getData(NodeRef nodeRef) {

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        EcosFormModel model = new EcosFormModel();

        model.setId((String) properties.get(EcosContentModel.PROP_ID));
        model.setTitle((String) properties.get(ContentModel.PROP_TITLE));
        model.setFormKey((String) properties.get(EFormModel.PROP_FORM_KEY));
        model.setDescription((String) properties.get(ContentModel.PROP_DESCRIPTION));
        model.setCustomModule((String) properties.get(EFormModel.PROP_CUSTOM_MODULE));

        return model;
    }
}
