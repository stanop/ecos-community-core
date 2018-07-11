package ru.citeck.ecos.invariants.view.forms;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.forms.FormMode;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class WithoutSavingTypeFormProvider extends TypeFormProvider implements WithoutSavingProvider {

    private static final String TYPE = "withoutSavingType";

    @Autowired
    private ServiceRegistry  serviceRegistry;
    private NamespaceService namespaceService;

    @PostConstruct
    private void postConstruct() {
        namespaceService = serviceRegistry.getNamespaceService();
    }

    @Override
    public Map<String, Object> saveNodeView(String formKey, String formId, FormMode mode, Map<String, Object> params,
                                            Map<QName, Object> attributes) {

        return WithoutSavingProvider.super.process(attributes, namespaceService);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}