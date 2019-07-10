package ru.citeck.ecos.records.source.common;

import org.alfresco.service.cmr.repository.MLText;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;

import java.util.Locale;

public class MLTextValue implements MetaValue {

    private MLText text;

    public MLTextValue(MLText text) {
        this.text = text;
    }

    @Override
    public String getString() {
        return text.getClosestValue(I18NUtil.getLocale());
    }

    @Override
    public Object getAttribute(String name, MetaField field) {
        return text.getClosestValue(new Locale(name));
    }

    @Override
    public boolean has(String name) {
        return text.containsKey(new Locale(name));
    }
}
