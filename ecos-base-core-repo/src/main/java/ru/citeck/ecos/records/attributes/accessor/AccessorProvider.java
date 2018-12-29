package ru.citeck.ecos.records.attributes.accessor;

import ru.citeck.ecos.records.attributes.AttributeAccessor;

import java.util.List;

public interface AccessorProvider {

    String getId();

    AttributeAccessor getAccessor(List<String> args, AttributeAccessor internal);
}
