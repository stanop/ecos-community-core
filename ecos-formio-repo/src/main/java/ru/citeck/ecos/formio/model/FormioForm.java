package ru.citeck.ecos.formio.model;

import com.fasterxml.jackson.databind.JsonNode;
import ru.citeck.ecos.formio.FormMode;
import ru.citeck.ecos.records.RecordRef;

public class FormioForm {

    private final RecordRef id;
    private final FormioFormModel model;

    public FormioForm(RecordRef id, FormioFormModel model) {
        this.model = model;
        this.id = id;
    }

    public String getId() {
        return id.toString();
    }

    public FormMode getFormMode() {
        return model.getFormMode();
    }

    public String getFormKey() {
        return model.getFormKey();
    }

    public JsonNode getDefinition() {
        return model.getDefinition();
    }

    public String getCustomModule() {
        return model.getCustomModule();
    }
}
