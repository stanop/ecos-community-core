package ru.citeck.ecos.records.source.alf;

import lombok.Getter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.records.source.MetaAttributeDef;

public class AlfAttributeDefinition implements MetaAttributeDef {

    private String name;

    private MessageService messageService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    @Getter(lazy = true)
    private final QName qname = resolveQName();
    @Getter(lazy = true)
    private final ClassAttributeDefinition definition = evalAttDefinition();

    public AlfAttributeDefinition(String name,
                                  NamespaceService namespaceService,
                                  DictionaryService dictionaryService,
                                  MessageService messageService) {
        this.name = name;
        this.namespaceService = namespaceService;
        this.dictionaryService = dictionaryService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return getDefinition().getTitle(messageService);
    }

    @Override
    public Class<?> getJavaClass() {
        ClassAttributeDefinition definition = getDefinition();
        if (definition instanceof PropertyDefinition) {
            PropertyDefinition propDef = (PropertyDefinition) definition;
            Class type;
            String className = propDef.getDataType().getJavaClassName();
            if (StringUtils.isNotBlank(className)) {
                try {
                    type = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    type = Object.class;
                }
            } else {
                type = Object.class;
            }
            return type;
        } else if (definition instanceof AssociationDefinition) {
            return NodeRef.class;
        }
        return Object.class;
    }

    @Override
    public boolean isMultiple() {

        ClassAttributeDefinition definition = getDefinition();

        if (definition instanceof PropertyDefinition) {
            PropertyDefinition propDef = (PropertyDefinition) definition;
            return propDef.isMultiValued();
        }
        if (definition instanceof AssociationDefinition) {
            AssociationDefinition assocDef = (AssociationDefinition) definition;
            return assocDef.isTargetMany();
        }
        return false;
    }

    private ClassAttributeDefinition evalAttDefinition() {
        PropertyDefinition propDef = dictionaryService.getProperty(getQname());
        if (propDef != null) {
            return propDef;
        }
        return dictionaryService.getAssociation(getQname());
    }

    private QName resolveQName() {
        return QName.resolveToQName(namespaceService, name);
    }
}
