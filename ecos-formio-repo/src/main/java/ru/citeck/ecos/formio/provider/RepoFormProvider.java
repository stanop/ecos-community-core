package ru.citeck.ecos.formio.provider;

import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.RepoContentDAO;
import ru.citeck.ecos.formio.model.FormioForm;
import ru.citeck.ecos.formio.model.FormioFormModel;
import ru.citeck.ecos.model.EcosFormioModel;
import ru.citeck.ecos.records.RecordRef;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RepoFormProvider extends AbstractFormProvider {

    private RepoContentDAO<FormioFormModel> formsContentDAO;

    @Override
    public FormioForm getForm(String formKey) {

        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(EcosFormioModel.PROP_FORM_KEY, formKey);

        Optional<ContentData<FormioFormModel>> form = formsContentDAO.getFirstContentData(keys);
        FormioForm formioForm = null;

        if (form.isPresent()) {

            Optional<FormioFormModel> formData = form.get().getData();
            RecordRef formRecordRef = new RecordRef(form.get().getNodeRef());
            formioForm = new FormioForm(formRecordRef, formData.orElse(null));
        }

        return formioForm;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public void setFormsContentDAO(RepoContentDAO<FormioFormModel> formsContentDAO) {
        this.formsContentDAO = formsContentDAO;
    }
}
