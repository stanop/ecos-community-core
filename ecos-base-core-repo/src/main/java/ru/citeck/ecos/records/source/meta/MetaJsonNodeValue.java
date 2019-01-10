package ru.citeck.ecos.records.source.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.source.alfnode.meta.AlfNodeAttValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetaJsonNodeValue implements MetaValue {

    private String id;
    private JsonNode data;
    private GqlContext context;

    public MetaJsonNodeValue(String id, JsonNode data) {
        this.id = id;
        this.data = data;
    }

    @Override
    public MetaValue init(GqlContext context) {
        this.context = context;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getString() {
        if (data == null || data instanceof NullNode) {
            return null;
        }
        return data.asText();
    }

    @Override
    public List<MetaValue> getAttribute(String name) {

        JsonNode attNode = data.get(name);

        if (attNode == null) {
            return Collections.emptyList();
        }

        return getValue(attNode);
    }

    private List<MetaValue> getValue(JsonNode attNode) {

        List<MetaValue> attValue = new ArrayList<>();

        if (attNode instanceof ArrayNode) {
            ArrayNode array = (ArrayNode) attNode;
            for (int i = 0; i < array.size(); i++) {
                attValue.addAll(getValue(array.get(i)));
            }
        } else {
            if (attNode instanceof ObjectNode) {
                JsonNode nodeRefNode = attNode.get("nodeRef");
                if (nodeRefNode instanceof TextNode) {
                    NodeRef nodeRef = new NodeRef(nodeRefNode.asText());
                    attValue.add(new AlfNodeAttValue(nodeRef).init(context));
                } else {
                    JsonNode qnameNode = attNode.get("fullQName");
                    if (qnameNode instanceof TextNode) {
                        QName qName = QName.createQName(qnameNode.asText());
                        attValue.add(new AlfNodeAttValue(qName).init(context));
                    }
                }
            }
            if (attValue.isEmpty()) {
                attValue.add(new MetaJsonNodeValue(null, attNode));
            }
        }

        return attValue;
    }
}
