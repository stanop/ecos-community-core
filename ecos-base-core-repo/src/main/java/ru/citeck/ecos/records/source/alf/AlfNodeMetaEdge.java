package ru.citeck.ecos.records.source.alf;

import lombok.Getter;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.EdgeOption;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.graphql.meta.value.SimpleMetaEdge;

import java.util.ArrayList;
import java.util.List;

public class AlfNodeMetaEdge extends SimpleMetaEdge {

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private MessageService messageService;

    @Getter(lazy = true)
    private final ClassAttributeDefinition definition = evalDefinition();

    public AlfNodeMetaEdge(AlfGqlContext context,
                           String name,
                           MetaValue scope) {
        super(name, scope);
        dictionaryService = context.getDictionaryService();
        namespaceService = context.getNamespaceService();
        messageService = context.getMessageService();
    }

    @Override
    public List<EdgeOption> getOptions() {

        ClassAttributeDefinition definition = getDefinition();

        if (definition instanceof PropertyDefinition) {

            List<ConstraintDefinition> constraints = ((PropertyDefinition) definition).getConstraints();

            for (ConstraintDefinition constraintDef : constraints) {

                Constraint constraint = constraintDef.getConstraint();

                if (constraint instanceof ListOfValuesConstraint) {

                    List<EdgeOption> options = new ArrayList<>();
                    ListOfValuesConstraint constraintList = (ListOfValuesConstraint) constraint;

                    for (String value : constraintList.getAllowedValues()) {

                        String display = constraintList.getDisplayLabel(value, messageService);
                        options.add(new EdgeOption(display, value));
                    }

                    return options;
                }
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
