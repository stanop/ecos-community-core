package ru.citeck.ecos.formio.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

public class FormioFormModel {

    @Getter @Setter private String id;
    @Getter @Setter private String title;
    @Getter @Setter private String description;
    @Getter @Setter private String formKey;
    @Deprecated
    @Getter @Setter private String formMode;
    @Getter @Setter private String customModule;
    @Getter @Setter private JsonNode definition;
}
