package ru.citeck.ecos.graphql.meta.value;

import org.alfresco.util.ISO8601DateFormat;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.MetaUtils;
import ru.citeck.ecos.graphql.node.GqlQName;

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
    public String getId(GqlContext context) {
        if (metaVal != null) {
            return metaVal.getId(context);
        }
        return null;
    }

    @Override
    public String getString(GqlContext context) {
        if (metaVal != null) {
            return metaVal.getString(context);
        } else if (val != null) {
            if (val instanceof Date) {
                return ISO8601DateFormat.format((Date) val);
            }
            return val.toString();
        }
        return null;
    }

    @Override
    public List<MetaValue> getAttribute(String name, GqlContext context) {
        if (metaVal != null) {
            return metaVal.getAttribute(name, context);
        }
        return MetaUtils.getReflectionValue(val, name);
    }
}
