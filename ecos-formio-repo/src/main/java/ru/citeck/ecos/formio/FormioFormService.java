package ru.citeck.ecos.formio;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
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

    public FormioForm createForm(String id, Map<String, String> properties) {

        Map<QName, Serializable> qnameProps = new HashMap<>();
        properties.forEach((name, value) -> {
            QName prop = null;
            switch (name) {
                case "formType":
                    prop = EcosFormioModel.PROP_FORM_TYPE;
                    break;
                case "formKey":
                    prop = EcosFormioModel.PROP_FORM_KEY;
                    break;
                case "formId":
                    prop = EcosFormioModel.PROP_FORM_ID;
                    break;
                case "formMode":
                    prop = EcosFormioModel.PROP_FORM_MODE;
                    break;
            }
            if (prop != null) {
                qnameProps.put(prop, value);
            }
        });

        qnameProps.put(EcosFormioModel.PROP_ID, id);
        qnameProps.put(ContentModel.PROP_NODE_UUID, id);

        NodeRef result = formsContentDAO.createNode(qnameProps);
/*
        FormioForm nodeRef*/

        return null;
    }

    public Optional<FormioForm> getForm(String id) {

        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(EcosFormioModel.PROP_ID, id);

        return formsContentDAO.getFirstContentData(keys).flatMap(ContentData::getData);
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
