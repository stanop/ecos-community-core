package ru.citeck.ecos.formio.provider;

import org.alfresco.service.namespace.QName;
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
    public FormioForm getForm(String formKey, Boolean isViewMode) {

        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(EcosFormioModel.PROP_FORM_KEY, formKey);
        if (isViewMode != null) {
            keys.put(EcosFormioModel.PROP_IS_VIEW_FORM, isViewMode);
        }

        return getForm(keys);
    }

    private FormioForm getForm(Map<QName, Serializable> keys) {

        Optional<ContentData<FormioFormModel>> form = formsContentDAO.getFirstContentData(keys);
        FormioForm formioForm = null;

        if (form.isPresent()) {

            Optional<FormioFormModel> formData = form.get().getData();
            RecordRef formRecordRef = new RecordRef(form.get().getNodeRef());
            formioForm = new FormioForm(formRecordRef, formData.orElse(null));

        } else if (keys.containsKey(EcosFormioModel.PROP_IS_VIEW_FORM)) {

            keys.remove(EcosFormioModel.PROP_IS_VIEW_FORM);
            return getForm(keys);
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
