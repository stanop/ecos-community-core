package ru.citeck.ecos.utils.json.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public abstract class NodeRefMixIn {
    @JsonCreator
    public NodeRefMixIn(String value) {}
    @JsonValue
    public abstract String toString();
}
