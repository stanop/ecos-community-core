package ru.citeck.ecos.formio;

import ru.citeck.ecos.formio.model.FormioForm;
import ru.citeck.ecos.formio.provider.FormProvider;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class FormioFormServiceImpl implements FormioFormService {

    private Set<FormProvider> providers = new TreeSet<>(Comparator.comparing(FormProvider::getOrder));

    @Override
    public Optional<FormioForm> getForm(String formKey, FormMode mode) {

        FormioForm form = null;

        for (FormProvider provider : providers) {
            form = provider.getForm(formKey, mode);
            if (form != null) {
                break;
            }
        }

        return Optional.ofNullable(form);
    }

    @Override
    public void register(FormProvider formProvider) {
        providers.add(formProvider);
    }
}
