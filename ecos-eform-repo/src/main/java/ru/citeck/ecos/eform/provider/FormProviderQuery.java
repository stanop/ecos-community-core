package ru.citeck.ecos.eform.provider;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class FormProviderQuery {

    private String formKey;
    private String formId;

    private ObjectNode params;

    public ObjectNode getParams() {
        return params;
    }

    public void setParams(ObjectNode params) {
        this.params = params;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }
}
