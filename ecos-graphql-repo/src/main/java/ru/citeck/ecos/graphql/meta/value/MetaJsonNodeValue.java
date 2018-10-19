package ru.citeck.ecos.graphql.meta.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.alfnode.AlfNodeAttValue;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;
import ru.citeck.ecos.graphql.meta.attribute.MetaExplicitAtt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MetaJsonNodeValue implements MetaValue {

    private String id;
    private JsonNode data;
    private GqlContext context;

    public MetaJsonNodeValue(String id, JsonNode data) {
        this.id = id;
        this.data = data;
    }

    public MetaJsonNodeValue(String id, JsonNode data, GqlContext context) {
        this.id = id;
        this.data = data;
        this.context = context;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String str() {
        if (data == null || data instanceof NullNode) {
            return null;
        }
        return data.asText();
    }

    @Override
    public Optional<MetaAttribute> att(String name) {

        JsonNode attNode = data.get(name);

        if (attNode == null) {
            return Optional.empty();
        }

        MetaAttribute attribute = new MetaExplicitAtt(name, getValue(attNode));
        return Optional.of(attribute);
    }

    private List<MetaValue> getValue(JsonNode attNode) {

        List<MetaValue> attValue = new ArrayList<>();

        if (attNode instanceof ArrayNode) {
            ArrayNode array = (ArrayNode) attNode;
            for (int i = 0; i < array.size(); i++) {
                attValue.addAll(getValue(array.get(i)));
            }
        } else {
            if (attNode instanceof ObjectNode && context != null) {
                JsonNode nodeRefNode = attNode.get("nodeRef");
                if (nodeRefNode instanceof TextNode) {
                    NodeRef nodeRef = new NodeRef(nodeRefNode.asText());
                    attValue.add(new AlfNodeAttValue(nodeRef, context));
                } else {
                    JsonNode qnameNode = attNode.get("fullQName");
                    if (qnameNode instanceof TextNode) {
                        QName qName = QName.createQName(qnameNode.asText());
                        attValue.add(new AlfNodeAttValue(qName, context));
                    }
                }
            }
            if (attValue.isEmpty()) {
                attValue.add(new MetaJsonNodeValue(null, attNode, context));
            }
        }

        return attValue;
    }

    @Override
    public List<MetaAttribute> atts(String filter) {
        return Collections.emptyList();
    }
}
