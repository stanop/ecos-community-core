package ru.citeck.ecos.eform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.records2.graphql.meta.annotation.DisplayName;

public class EcosFormModel {

    @Getter @Setter private String id;
    @Getter @Setter private String title;
    @Getter @Setter private String description;
    @Getter @Setter private String formKey;
    @Deprecated
    @Getter @Setter private String formMode;
    @Getter @Setter private String customModule;
    @Getter @Setter private JsonNode definition;
    @Getter @Setter private ObjectNode i18n;

    public EcosFormModel() {
    }

    public EcosFormModel(EcosFormModel model) {
        this.id = model.getId();
        this.title = model.getTitle();
        this.description = model.getDescription();
        this.formKey = model.getFormKey();
        this.customModule = model.getCustomModule();
        this.definition = model.getDefinition();
        this.i18n = model.getI18n();
    }

    @DisplayName
    @JsonIgnore
    public String getDisplayName() {
        if (title != null) {
            return title;
        }
        return I18NUtil.getMessage("ecosForms_model.type.ecosForms_form.title");
    }
}
