package ru.citeck.ecos.graphql.meta.value;

import org.alfresco.util.ISO8601DateFormat;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;
import ru.citeck.ecos.graphql.meta.attribute.MetaReflectionAtt;

import java.util.Collections;
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
    public String id() {
        if (metaVal != null) {
            return metaVal.id();
        }
        return null;
    }

    @Override
    public String str() {
        if (metaVal != null) {
            return metaVal.str();
        } else if (val != null) {
            if (val instanceof Date) {
                return ISO8601DateFormat.format((Date) val);
            }
            return val.toString();
        }
        return null;
    }

    @Override
    public Optional<MetaAttribute> att(String name) {
        if (metaVal != null) {
            return metaVal.att(name);
        }
        return Optional.of(new MetaReflectionAtt(val, name));
    }

    @Override
    public List<MetaAttribute> atts(String filter) {
        if (metaVal != null) {
            return metaVal.atts(filter);
        }
        return Collections.emptyList();
    }
}
