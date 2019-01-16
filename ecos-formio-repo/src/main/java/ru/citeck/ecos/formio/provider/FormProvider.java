package ru.citeck.ecos.formio.provider;

import ru.citeck.ecos.formio.model.FormioForm;

import java.util.Optional;

public interface FormProvider {

    FormioForm getForm(String formKey);

    int getOrder();
}
