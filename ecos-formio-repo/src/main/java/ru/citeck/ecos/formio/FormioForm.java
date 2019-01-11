package ru.citeck.ecos.formio;

import com.fasterxml.jackson.databind.JsonNode;

public class FormioForm {

    private String id;

    private String title;
    private String description;

    private String formType;
    private String formKey;
    private String formId;
    private String formMode;

    private JsonNode definition;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
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

    public String getFormMode() {
        return formMode;
    }

    public void setFormMode(String formMode) {
        this.formMode = formMode;
    }

    public JsonNode getDefinition() {
        return definition;
    }

    public void setDefinition(JsonNode definition) {
        this.definition = definition;
    }
}
