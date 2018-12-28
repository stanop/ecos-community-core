package ru.citeck.ecos.records.attribute.accessor;

import ru.citeck.ecos.records.attribute.AttributeAccessor;

import java.util.List;

public interface AccessorProvider {

    String getId();

    AttributeAccessor getAccessor(List<String> args, AttributeAccessor internal);
}
