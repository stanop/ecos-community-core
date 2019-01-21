package ru.citeck.ecos.formio.provider;

import ru.citeck.ecos.formio.FormMode;
import ru.citeck.ecos.formio.model.FormioForm;

public interface FormProvider {

    FormioForm getForm(String formKey, FormMode mode);

    int getOrder();
}
