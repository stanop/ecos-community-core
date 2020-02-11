package ru.citeck.ecos.icase.activity.create.provider.impl;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.icase.activity.create.CreateVariantsProviderRegistry;
import ru.citeck.ecos.icase.activity.create.dto.ActivityCreateVariant;
import ru.citeck.ecos.icase.activity.create.provider.CreateVariantsProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuCreateVariantsProvider implements CreateVariantsProvider {

    private List<QName> createMenuTypes = Collections.emptyList();

    private CreateVariantsProviderRegistry createVariantsProviderRegistry;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private MessageService messageService;

    @Override
    public List<ActivityCreateVariant> getCreateVariants() {

        List<ActivityCreateVariant> variants = new ArrayList<>();
        for (QName menuType : createMenuTypes) {
            ActivityCreateVariant variant = new ActivityCreateVariant();
            variant.setTitle(dictionaryService.getType(menuType).getTitle(messageService));
            variant.setType(menuType);
            variant.setId(menuType.toPrefixString(namespaceService));
            variant.setCanBeCreated(false);
            variants.add(variant);
        }

        List<CreateVariantsProvider> createVariantsProviders =
            createVariantsProviderRegistry.getCreateVariantsProviders();

        for (CreateVariantsProvider provider : createVariantsProviders) {
            for (ActivityCreateVariant variant : provider.getCreateVariants()) {
                for (ActivityCreateVariant baseVariant : variants) {
                    if (dictionaryService.isSubClass(variant.getType(), baseVariant.getType())) {
                        baseVariant.addChild(variant);
                        break;
                    }
                }
            }
        }

        return variants;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        dictionaryService = serviceRegistry.getDictionaryService();
        namespaceService = serviceRegistry.getNamespaceService();
        messageService = serviceRegistry.getMessageService();
    }

    public void setCreateMenuTypes(List<QName> createMenuTypes) {
        if (CollectionUtils.isNotEmpty(createMenuTypes)) {
            this.createMenuTypes = createMenuTypes;
        }
    }

    @Autowired
    public void setCreateVariantsProviderRegistry(CreateVariantsProviderRegistry createVariantsProviderRegistry) {
        this.createVariantsProviderRegistry = createVariantsProviderRegistry;
    }
}
