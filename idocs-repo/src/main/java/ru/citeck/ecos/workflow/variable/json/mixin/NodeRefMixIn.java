package ru.citeck.ecos.workflow.variable.json.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public abstract class NodeRefMixIn {
    @JsonCreator
    public NodeRefMixIn(String value) {}
    @JsonValue
    public abstract String toString();
}
