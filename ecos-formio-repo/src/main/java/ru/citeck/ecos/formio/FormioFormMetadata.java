package ru.citeck.ecos.formio;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.content.metadata.MetadataExtractor;
import ru.citeck.ecos.model.EcosFormioModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FormioFormMetadata implements MetadataExtractor<FormioForm> {

    @Override
    public Map<QName, Serializable> getMetadata(FormioForm form) {
        Map<QName, Serializable> metadata = new HashMap<>();
        metadata.put(EcosFormioModel.PROP_ID, form.getId());
        metadata.put(EcosFormioModel.PROP_FORM_ID, form.getFormId());
        metadata.put(EcosFormioModel.PROP_FORM_KEY, form.getFormKey());
        metadata.put(EcosFormioModel.PROP_FORM_MODE, form.getFormMode());
        metadata.put(EcosFormioModel.PROP_FORM_TYPE, form.getFormType());
        return metadata;
    }
}
