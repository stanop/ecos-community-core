package ru.citeck.ecos.formio;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.RepoContentDAO;
import ru.citeck.ecos.model.EcosFormioModel;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FormioFormService {

    private RepoContentDAO<FormioForm> formsContentDAO;

    @PostConstruct
    public void init() {

    }

    public Optional<FormioForm> getForm(String type, String key, String id, String mode) {

        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(EcosFormioModel.PROP_FORM_TYPE, type);
        keys.put(EcosFormioModel.PROP_FORM_KEY, key);
        keys.put(EcosFormioModel.PROP_FORM_ID, id);
        keys.put(EcosFormioModel.PROP_FORM_MODE, mode);

        Optional<FormioForm> form = formsContentDAO.getFirstContentData(keys).flatMap(ContentData::getData);
        if (!form.isPresent() && mode != null) {
            form = getForm(type, key, id, null);
        }

        return form;
    }

    public void setFormsContentDAO(RepoContentDAO<FormioForm> formsContentDAO) {
        this.formsContentDAO = formsContentDAO;
    }
}
