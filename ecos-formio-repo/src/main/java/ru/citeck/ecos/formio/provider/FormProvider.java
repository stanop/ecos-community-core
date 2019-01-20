package ru.citeck.ecos.formio.provider;

import ru.citeck.ecos.formio.model.FormioForm;

public interface FormProvider {

    FormioForm getForm(String formKey, Boolean isViewMode);

    int getOrder();
}
