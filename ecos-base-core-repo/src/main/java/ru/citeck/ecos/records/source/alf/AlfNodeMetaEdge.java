package ru.citeck.ecos.records.source.alf;

import lombok.Getter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.graphql.meta.value.SimpleMetaEdge;
import ru.citeck.ecos.security.EcosPermissionService;
import ru.citeck.ecos.utils.DictUtils;
import ru.citeck.ecos.utils.NodeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlfNodeMetaEdge extends SimpleMetaEdge {

    private static final NodeRef TYPES_ROOT = new NodeRef("workspace://SpacesStore/category-document-type-root");

    private MessageService messageService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    private DictUtils dictUtils;
    private EcosPermissionService ecosPermissionService;

    private QName scopeType;
    private MetaValue scope;
    private AlfGqlContext context;

    @Getter(lazy = true)
    private final NodeRef nodeRef = evalNodeRef();

    @Getter(lazy = true)
    private final ClassAttributeDefinition definition = evalDefinition();

    public AlfNodeMetaEdge(AlfGqlContext context,
                           QName scopeType,
                           String name,
                           MetaValue scope) {
        super(name, scope);

        this.scope = scope;
        this.context = context;

        messageService = context.getMessageService();
        namespaceService = context.getNamespaceService();
        dictionaryService = context.getDictionaryService();

        dictUtils = context.getService(DictUtils.QNAME);
        ecosPermissionService = context.getService(EcosPermissionService.QNAME);

        this.scopeType = scopeType;
    }

    @Override
    public List<AttOption> getOptions() {

        ClassAttributeDefinition definition = getDefinition();

        if (definition instanceof PropertyDefinition) {

            List<AttOption> options = new ArrayList<>();

            Map<String, String> mapping = dictUtils.getPropertyDisplayNameMapping(scopeType, definition.getName());

            if (mapping != null && mapping.size() > 0) {
                mapping.forEach((value, title) ->
                    options.add(new AttOption(value, title))
                );
            } else if ("tk:type".equals(getName())) {

                NodeUtils nodeUtils = context.getService(NodeUtils.QNAME);

                List<NodeRef> types = nodeUtils.getAssocTargets(TYPES_ROOT, ContentModel.ASSOC_SUBCATEGORIES);
                types.forEach(t -> options.add(new AttOption(t.toString(), nodeUtils.getDisplayName(t))));
            }

            if (!options.isEmpty()) {
                return options;
            }
        }

        return null;
    }

    @Override
    public boolean isAssociation() {
        return getDefinition() instanceof AssociationDefinition;
    }

    @Override
    public boolean isProtected() {

        NodeRef nodeRef = getNodeRef();
        if (nodeRef == null || ecosPermissionService == null) {
            return false;
        }

        return ecosPermissionService.isAttributeProtected(nodeRef, getName());
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
        return definition != null ? definition.getTitle(messageService) : getName();
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

    @Override
    public String getEditorKey() {

        ClassAttributeDefinition definition = getDefinition();

        if (definition instanceof AssociationDefinition) {

            ClassDefinition targetClass = ((AssociationDefinition) definition).getTargetClass();

            return "alf_" + targetClass.getName().toPrefixString(namespaceService);
        }

        return null;
    }

    @Override
    public String getType() {

        ClassAttributeDefinition definition = getDefinition();

        if (definition instanceof PropertyDefinition) {

            DataTypeDefinition dataType = ((PropertyDefinition) definition).getDataType();
            QName typeName = dataType.getName();

            if (DataTypeDefinition.TEXT.equals(typeName)) {

                List<AttOption> options = getOptions();
                if (options != null && !options.isEmpty()) {
                    return "options";
                }
            }

            return typeName != null ? typeName.getLocalName() : DataTypeDefinition.TEXT.getLocalName();

        } else if (definition instanceof AssociationDefinition) {

            ClassDefinition targetClass = ((AssociationDefinition) definition).getTargetClass();
            QName name = targetClass.getName();

            if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(name)) {
                return "authorityGroup";
            } else if (ContentModel.TYPE_AUTHORITY.equals(name)) {
                return "authority";
            } else if (ContentModel.TYPE_PERSON.equals(name)) {
                return "person";
            }

            return "assoc";
        }

        return DataTypeDefinition.TEXT.getLocalName();
    }

    private ClassAttributeDefinition evalDefinition() {

        QName qName = QName.resolveToQName(namespaceService, getName());
        if (qName == null) {
            return null;
        }

        PropertyDefinition property = dictUtils.getPropDef(scopeType, qName);

        if (property != null) {
            return property;
        }
        return dictionaryService.getAssociation(qName);
    }

    private NodeRef evalNodeRef() {

        if (scope == null) {
            return null;
        }

        String id = scope.getId();
        if (id == null || !id.startsWith("workspace://")) {
            return null;
        }

        return new NodeRef(id);
    }

    public static class AttOption implements MetaValue {

        private String value;
        private String title;

        public AttOption(String value, String title) {
            this.value = value;
            this.title = title;
        }

        @Override
        public String getString() {
            return value;
        }

        @Override
        public String getDisplayName() {
            return title;
        }
    }
}
