package ru.citeck.ecos.records.source.alf;

import lombok.Getter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.EdgeOption;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.graphql.meta.value.SimpleMetaEdge;
import ru.citeck.ecos.utils.DictUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlfNodeMetaEdge extends SimpleMetaEdge {

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private MessageService messageService;
    private DictUtils dictUtils;

    @Getter(lazy = true)
    private final ClassAttributeDefinition definition = evalDefinition();

    public AlfNodeMetaEdge(AlfGqlContext context,
                           String name,
                           MetaValue scope) {
        super(name, scope);
        dictionaryService = context.getDictionaryService();
        namespaceService = context.getNamespaceService();
        messageService = context.getMessageService();
        dictUtils = context.getService(DictUtils.QNAME);
    }

    @Override
    public List<EdgeOption> getOptions() {

        ClassAttributeDefinition definition = getDefinition();

        if (definition instanceof PropertyDefinition) {

            Map<String, String> mapping = dictUtils.getPropertyDisplayNameMapping(definition.getName());

            if (mapping != null && mapping.size() > 0) {
                List<EdgeOption> options = new ArrayList<>();
                mapping.forEach((value, title) -> {
                    options.add(new EdgeOption(title, value));
                });
                return options;
            }
        }

        return null;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isMultiple() {

        ClassAttributeDefinition definition = getDefinition();
        if (definition instanceof PropertyDefinition) {
            return ((PropertyDefinition) definition).isMultiValued();
        } else if (definition instanceof AssociationDefinition) {
            return ((AssociationDefinition) definition).isTargetMany();
        }

        return false;
    }

    @Override
    public String getTitle() {
        ClassAttributeDefinition definition = getDefinition();
        return definition.getTitle(messageService);
    }

    @Override
    public String getDescription() {
        ClassAttributeDefinition definition = getDefinition();
        return definition.getDescription(messageService);
    }

    @Override
    public Class<?> getJavaClass() {

        ClassAttributeDefinition definition = getDefinition();

        if (definition instanceof PropertyDefinition) {

            DataTypeDefinition dataType = ((PropertyDefinition) definition).getDataType();
            try {
                return Class.forName(dataType.getJavaClassName());
            } catch (ClassNotFoundException e) {
                //do nothing
            }
        } else if (definition instanceof AssociationDefinition) {
            return NodeRef.class;
        }

        return null;
    }

    private ClassAttributeDefinition evalDefinition() {

        QName qName = QName.resolveToQName(namespaceService, getName());
        if (qName == null) {
            return null;
        }

        PropertyDefinition property = dictionaryService.getProperty(qName);
        if (property != null) {
            return property;
        }
        return dictionaryService.getAssociation(qName);
    }
}
