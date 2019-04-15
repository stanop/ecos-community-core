package ru.citeck.ecos.eform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
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

    public EcosFormModel() {
    }

    public EcosFormModel(EcosFormModel model) {
        this.id = model.getId();
        this.title = model.getTitle();
        this.description = model.getDescription();
        this.formKey = model.getFormKey();
        this.customModule = model.getCustomModule();
        this.definition = model.getDefinition();
    }

    @DisplayName
    @JsonIgnore
    public String getDisplayName() {
        return title;
    }
}
