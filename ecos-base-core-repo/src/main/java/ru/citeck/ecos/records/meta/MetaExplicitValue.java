package ru.citeck.ecos.records.meta;

import org.alfresco.util.ISO8601DateFormat;
import ru.citeck.ecos.records2.graphql.GqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;

import java.util.Date;
import java.util.Optional;

public class MetaExplicitValue implements MetaValue {

    private Object val;
    private MetaValue metaVal;

    public MetaExplicitValue(Object value) {
        if (value instanceof Optional) {
            val = ((Optional<?>) value).orElse(null);
        } else {
            val = value;
        }
        if (val instanceof MetaValue) {
            metaVal = (MetaValue) val;
        }
    }

    @Override
    public <T extends GqlContext> void init(T context, MetaField field) {
        if (metaVal != null) {
            metaVal.init(context, field);
        }
    }

    @Override
    public String getId() {
        if (metaVal != null) {
            return metaVal.getId();
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        if (metaVal != null) {
            return metaVal.getDisplayName();
        }
        return getString();
    }

    @Override
    public String getString() {
        if (metaVal != null) {
            return metaVal.getString();
        } else if (val != null) {
            if (val instanceof Date) {
                return ISO8601DateFormat.format((Date) val);
            }
            return val.toString();
        }
        return null;
    }

    @Override
    public Object getAttribute(String name, MetaField field) throws Exception {
        if (metaVal != null) {
            return metaVal.getAttribute(name, field);
        }
        return MetaUtils.getReflectionValue(val, name);
    }
}

