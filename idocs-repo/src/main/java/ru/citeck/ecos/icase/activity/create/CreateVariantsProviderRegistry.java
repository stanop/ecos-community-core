package ru.citeck.ecos.icase.activity.create;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.create.provider.CreateVariantsProvider;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateVariantsProviderRegistry {

    private List<CreateVariantsProvider> createVariantsProviders = new ArrayList<>();

    public void registerCreateVariantsProvider(CreateVariantsProvider provider) {
        createVariantsProviders.add(provider);
    }

    public List<CreateVariantsProvider> getCreateVariantsProviders() {
        return createVariantsProviders;
    }

}
