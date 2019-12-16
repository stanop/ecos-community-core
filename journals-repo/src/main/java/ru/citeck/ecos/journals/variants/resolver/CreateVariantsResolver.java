package ru.citeck.ecos.journals.variants.resolver;

import ru.citeck.ecos.journals.CreateVariant;
import ru.citeck.ecos.records2.RecordRef;

import java.util.List;

public interface CreateVariantsResolver<T> {

    Class<T> getConfigType();

    String getId();

    List<CreateVariant> getCreateVariants(RecordRef recordRef, T config);
}
