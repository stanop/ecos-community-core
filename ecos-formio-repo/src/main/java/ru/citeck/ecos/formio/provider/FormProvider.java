package ru.citeck.ecos.formio.provider;

import ru.citeck.ecos.formio.model.FormioFormModel;

public interface FormProvider {

    FormioFormModel getFormByKey(String formKey);

    FormioFormModel getFormById(String id);

    int getOrder();
}
