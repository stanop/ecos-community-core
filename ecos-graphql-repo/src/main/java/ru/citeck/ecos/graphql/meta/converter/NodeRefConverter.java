package ru.citeck.ecos.graphql.meta.converter;

import com.fasterxml.jackson.databind.JsonNode;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Pavel Simonov
 */
public class NodeRefConverter extends MetaConverter<NodeRef> {

    @Override
    public NodeRef convert(JsonNode data) throws ReflectiveOperationException {
        JsonNode strValue = data.get(META_STR_FIELD);
        String str = strValue.isTextual() ? strValue.asText() : null;
        return str != null && NodeRef.isNodeRef(str) ? new NodeRef(str) : null;
    }

    @Override
    public StringBuilder appendQuery(StringBuilder query) {
        return query.append(META_STR_FIELD);
    }
}