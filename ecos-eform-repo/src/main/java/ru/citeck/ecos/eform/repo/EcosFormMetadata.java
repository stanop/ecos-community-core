package ru.citeck.ecos.eform.repo;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.content.metadata.MetadataExtractor;
import ru.citeck.ecos.eform.model.EcosFormModel;
import ru.citeck.ecos.model.EFormModel;
import ru.citeck.ecos.model.EcosContentModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EcosFormMetadata implements MetadataExtractor<EcosFormModel> {

    @Override
    public Map<QName, Serializable> getMetadata(EcosFormModel form) {

        Map<QName, Serializable> metadata = new HashMap<>();
        metadata.put(EcosContentModel.PROP_ID, form.getId());
        metadata.put(EFormModel.PROP_FORM_KEY, form.getFormKey());
        metadata.put(EFormModel.PROP_CUSTOM_MODULE, form.getCustomModule());
        metadata.put(ContentModel.PROP_DESCRIPTION, form.getDescription());
        metadata.put(ContentModel.PROP_TITLE, form.getTitle());

        return metadata;
    }
}
