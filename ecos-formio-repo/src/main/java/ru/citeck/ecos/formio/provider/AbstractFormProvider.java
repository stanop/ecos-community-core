package ru.citeck.ecos.formio.provider;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.formio.FormioFormService;

import javax.annotation.PostConstruct;

public abstract class AbstractFormProvider implements FormProvider {

    protected FormioFormService formService;

    @PostConstruct
    public void registerProvider() {
        formService.register(this);
    }

    @Autowired
    public void setFormService(FormioFormService formService) {
        this.formService = formService;
    }
}
