package ru.citeck.ecos.formio;

import ru.citeck.ecos.formio.model.FormioForm;
import ru.citeck.ecos.formio.provider.FormProvider;
import ru.citeck.ecos.model.EcosFormioModel;

import java.util.Optional;

public interface FormioFormService {

    Optional<FormioForm> getForm(String formKey, FormMode isViewMode);

    void register(FormProvider formProvider);
}
