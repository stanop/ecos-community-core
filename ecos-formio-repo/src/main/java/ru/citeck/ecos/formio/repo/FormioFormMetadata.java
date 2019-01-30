package ru.citeck.ecos.formio.repo;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.content.metadata.MetadataExtractor;
import ru.citeck.ecos.formio.FormMode;
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

        FormMode mode = form.getFormMode();
        metadata.put(EcosFormioModel.PROP_FORM_MODE, mode != null ? mode.toString() : null);

        return metadata;
    }
}
