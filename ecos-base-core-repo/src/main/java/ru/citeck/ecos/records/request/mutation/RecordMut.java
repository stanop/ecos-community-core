package ru.citeck.ecos.records.request.mutation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.citeck.ecos.records.RecordRef;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class RecordMut {

    private RecordRef id;
    private String type;
    private String parent;
    private String parentAtt;

    private ObjectNode attributes = JsonNodeFactory.instance.objectNode();

    public String getParentAtt() {
        return parentAtt;
    }

    public void setParentAtt(String parentAtt) {
        this.parentAtt = parentAtt;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public RecordRef getId() {
        return id;
    }

    public void setId(RecordRef id) {
        this.id = id;
    }

    public ObjectNode getAttributes() {
        return attributes;
    }

    public void setAttributes(ObjectNode attributes) {
        if (attributes != null) {
            this.attributes = attributes;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
