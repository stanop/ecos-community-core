package ru.citeck.ecos.workflow.variable.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class PojoVariableWrapper<T> {

    @JsonProperty("v")
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
                  include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                  property = "t")
    public final T variable;

    @JsonCreator
    public PojoVariableWrapper(@JsonProperty("v") T variable) {
        this.variable = variable;
    }
}
