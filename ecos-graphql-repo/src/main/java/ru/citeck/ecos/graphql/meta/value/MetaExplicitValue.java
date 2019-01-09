package ru.citeck.ecos.graphql.meta.value;

import org.alfresco.util.ISO8601DateFormat;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.MetaUtils;

import java.util.Date;
import java.util.List;
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
    public MetaValue init(GqlContext context) {
        if (metaVal != null) {
            metaVal.init(context);
        }
        return this;
    }

    @Override
    public String getId() {
        if (metaVal != null) {
            return metaVal.getId();
        }
        return null;
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
    public List<MetaValue> getAttribute(String name) {
        if (metaVal != null) {
            return metaVal.getAttribute(name);
        }
        return MetaUtils.getReflectionValue(val, name);
    }
}
