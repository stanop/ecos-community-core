package ru.citeck.ecos.eform.provider;

import ru.citeck.ecos.eform.model.EcosFormModel;

public interface FormProvider {

    EcosFormModel getFormByKey(String formKey);

    EcosFormModel getFormById(String id);

    int getOrder();
}
