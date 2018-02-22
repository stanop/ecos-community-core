package ru.citeck.ecos.workflow.variable.json.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.alfresco.service.namespace.QName;

public abstract class QNameMixIn {
    @JsonCreator
    public static QName createQName(String qname) {
        return null;
    }
    @JsonValue
    public abstract String toString();
}
