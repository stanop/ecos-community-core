package ru.citeck.ecos.eform.provider;

import ru.citeck.ecos.eform.model.EcosFormModel;

public interface MutableFormProvider {

    void save(EcosFormModel model);

    void create(EcosFormModel model);
}
