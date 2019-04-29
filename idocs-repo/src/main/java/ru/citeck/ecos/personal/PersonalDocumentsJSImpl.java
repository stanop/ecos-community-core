package ru.citeck.ecos.personal;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

import static ru.citeck.ecos.utils.JavaScriptImplUtils.wrapNode;

public class PersonalDocumentsJSImpl extends AlfrescoScopableProcessorExtension {

    PersonalDocumentsService personalDocumentsService;

    public ScriptNode ensureTempDirectory() {
        return wrapNode(personalDocumentsService.ensureTempDirectory(), this);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setPersonalDocumentsService(PersonalDocumentsService personalDocumentsService) {
        this.personalDocumentsService = personalDocumentsService;
    }

}
