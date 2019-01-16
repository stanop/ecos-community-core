package ru.citeck.ecos.formio;

import ru.citeck.ecos.formio.model.FormioForm;
import ru.citeck.ecos.formio.provider.FormProvider;

import java.util.Optional;

public interface FormioFormService {

    Optional<FormioForm> getForm(String formKey);

    void register(FormProvider formProvider);

}
