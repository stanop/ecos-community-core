package ru.citeck.ecos.user;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.AlfrescoMissingQNamesModel;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

import java.util.HashMap;
import java.util.Map;

public class EcosAuthorityServiceJs extends AlfrescoScopableProcessorExtension {

    private ServiceRegistry services;
    private EcosAuthorityService ecosAuthorityService;

    public ScriptNode createPerson(String lastName, String firstName, String middleName) {
        return createPerson(lastName, firstName, middleName, null);
    }

    public ScriptNode createPerson(String lastName, String firstName, String middleName, ScriptNode group) {
        Map<QName, Object> props = new HashMap<>();
        props.put(ContentModel.PROP_LASTNAME, lastName);
        props.put(ContentModel.PROP_FIRSTNAME, firstName);
        props.put(AlfrescoMissingQNamesModel.PROP_MIDDLE_NAME, middleName);
        return createPerson(props, group);
    }

    public ScriptNode createPerson(Map<QName, Object> properties) {
        return createPerson(properties, null);
    }

    public ScriptNode createPerson(Map<QName, Object> properties, ScriptNode group) {
        ScriptNode person = null;
        NodeRef personRef;
        if (group != null) {
            personRef = ecosAuthorityService.createPerson(properties, group.getNodeRef());
        } else {
            personRef = ecosAuthorityService.createPerson(properties);
        }
        person = new ScriptNode(personRef, services, getScope());
        return person;
    }

    public void setEcosAuthorityService(EcosAuthorityService ecosAuthorityService) {
        this.ecosAuthorityService = ecosAuthorityService;
    }

    public void setServices(ServiceRegistry services) {
        this.services = services;
    }
}

