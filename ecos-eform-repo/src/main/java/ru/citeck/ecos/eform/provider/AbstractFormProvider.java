package ru.citeck.ecos.eform.provider;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.eform.EcosFormService;

import javax.annotation.PostConstruct;

public abstract class AbstractFormProvider implements FormProvider {

    protected EcosFormService formService;

    @PostConstruct
    public void registerProvider() {
        formService.register(this);
    }

    @Autowired
    public void setFormService(EcosFormService formService) {
        this.formService = formService;
    }
}
