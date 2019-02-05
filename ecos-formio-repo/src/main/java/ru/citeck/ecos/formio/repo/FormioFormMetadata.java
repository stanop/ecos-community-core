package ru.citeck.ecos.formio.repo;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.content.metadata.MetadataExtractor;
import ru.citeck.ecos.formio.model.FormioFormModel;
import ru.citeck.ecos.model.EcosContentModel;
import ru.citeck.ecos.model.EcosFormioModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FormioFormMetadata implements MetadataExtractor<FormioFormModel> {

    @Override
    public Map<QName, Serializable> getMetadata(FormioFormModel form) {

        Map<QName, Serializable> metadata = new HashMap<>();
        metadata.put(EcosContentModel.PROP_ID, form.getId());
        metadata.put(EcosFormioModel.PROP_FORM_KEY, form.getFormKey());
        metadata.put(EcosFormioModel.PROP_CUSTOM_MODULE, form.getCustomModule());
        metadata.put(ContentModel.PROP_DESCRIPTION, form.getDescription());
        metadata.put(ContentModel.PROP_TITLE, form.getTitle());

        return metadata;
    }
}
