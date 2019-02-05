package ru.citeck.ecos.formio.provider;

import ru.citeck.ecos.formio.model.FormioFormModel;

public interface MutableFormProvider {

    void save(FormioFormModel model);

    void create(FormioFormModel model);
}
