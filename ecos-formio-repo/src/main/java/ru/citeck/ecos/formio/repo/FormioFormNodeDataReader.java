package ru.citeck.ecos.formio.repo;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.content.dao.NodeDataReader;
import ru.citeck.ecos.formio.model.FormioFormModel;
import ru.citeck.ecos.model.EcosContentModel;
import ru.citeck.ecos.model.EcosFormioModel;

import java.io.Serializable;
import java.util.Map;

public class FormioFormNodeDataReader implements NodeDataReader<FormioFormModel> {

    private NodeService nodeService;

    @Autowired
    public FormioFormNodeDataReader(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public FormioFormModel getData(NodeRef nodeRef) {

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        FormioFormModel model = new FormioFormModel();

        model.setId((String) properties.get(EcosContentModel.PROP_ID));
        model.setTitle((String) properties.get(ContentModel.PROP_TITLE));
        model.setFormKey((String) properties.get(EcosFormioModel.PROP_FORM_KEY));
        model.setIsViewForm((Boolean) properties.get(EcosFormioModel.PROP_IS_VIEW_FORM));
        model.setDescription((String) properties.get(ContentModel.PROP_DESCRIPTION));

        return model;
    }
}
