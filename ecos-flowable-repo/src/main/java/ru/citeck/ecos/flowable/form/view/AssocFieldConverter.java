package ru.citeck.ecos.flowable.form.view;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.flowable.form.model.FormField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.flowable.form.FlowableNodeViewProvider;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantConstants;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.invariants.view.NodeViewRegion;
import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.journals.JournalType;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AssocFieldConverter extends FieldConverter<FormField> {

    public static final String TYPE = "assoc_field";

    private static final String PEOPLE_FIELD = "people";
    private static final String GROUP_FIELD = "functional-group";

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private JournalService journalService;
    private AuthorityService authorityService;

    @Override
    protected Optional<NodeViewRegion> createInputRegion(FormField field, Map<String, Object> variables) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .name("input")
                                             .template("view")
                                             .build());
    }

    @Override
    protected Optional<NodeViewRegion> createSelectRegion(FormField field, Map<String, Object> variables) {

        String fieldName = field.getId()
                                .replace(FlowableNodeViewProvider.DOCUMENT_FIELD_PREFIX, "")
                                .replace('_', ':');

        QName assocQName = QName.resolveToQName(namespaceService, fieldName);

        AssociationDefinition assocDef = dictionaryService.getAssociation(assocQName);

        if (assocDef != null) {

            switch (field.getType()) {

                case PEOPLE_FIELD: {

                    return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                                         .name("select")
                                                         .template("select-orgstruct")
                                                         .build());
                }
                case GROUP_FIELD: {

                    Map<String, Object> params = Collections.singletonMap("allowedAuthorityType", "GROUP");
                    return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                                         .template("select-orgstruct")
                                                         .name("select")
                                                         .templateParams(params)
                                                         .build());
                }
                default:

                    ClassDefinition targetClass = assocDef.getTargetClass();
                    Optional<JournalType> journal = journalService.getJournalForType(targetClass.getName());

                    if (journal.isPresent()) {

                        Map<String, Object> params = new HashMap<>();
                        params.put("journalType", journal.get().getId());

                        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                                             .name("select")
                                                             .template("select-journal")
                                                             .templateParams(params)
                                                             .build());
                    }
            }
        }
        return Optional.empty();
    }

    @Override
    protected List<InvariantDefinition> getInvariants(FormField field,
                                                      QName fieldName,
                                                      Object defaultValue,
                                                      Map<String, Object> variables) {

        List<InvariantDefinition> invariants = super.getInvariants(field, fieldName, null, variables);

        InvariantDefinition.Builder invBuilder = new InvariantDefinition.Builder(prefixResolver);
        invBuilder.pushScope(fieldName, InvariantScope.AttributeScopeKind.PROPERTY);

        String fieldType = field.getType();

        boolean isAuthority = GROUP_FIELD.equals(fieldType) || PEOPLE_FIELD.equals(fieldType);

        if (!isAuthority) {

            invariants.add(invBuilder.feature(Feature.VALUE_TITLE)
                                     .language(InvariantConstants.LANGUAGE_JAVASCRIPT)
                                     .expression("value.properties['cm:title'] || value.properties['cm:name']")
                                     .build());
        } else {

            invariants.add(invBuilder.feature(Feature.VALUE_TITLE)
                    .language(InvariantConstants.LANGUAGE_JAVASCRIPT)
                    .expression(
                            "value.typeShort == 'cm:person' ? " +
                                    "value.impl().getAttribute('cm:firstName').value() + ' ' + " +
                                    "value.impl().getAttribute('cm:lastName').value() : " +
                                    "value.impl().getAttribute('cm:authorityDisplayName').value()")
                    .build());
        }

        final List<String> nodes = new ArrayList<>();

        if (defaultValue instanceof String) {
            nodes.add((String) defaultValue);
        } else if (defaultValue instanceof Collection) {
            ((Collection<?>) defaultValue).forEach(value -> {
                if (value instanceof NodeRef) {
                    nodes.add(((NodeRef) value).toString());
                } else if (value instanceof String) {
                    nodes.add((String) value);
                }
            });
        }

        if (!nodes.isEmpty()) {

            List<String> nodeRefs = nodes.stream()
                                         .map(n -> resolveNodeRef(n, isAuthority))
                                         .filter(Optional::isPresent)
                                         .map(Optional::get)
                                         .collect(Collectors.toList());

            if (!nodeRefs.isEmpty()) {
                invariants.add(invBuilder.feature(Feature.DEFAULT)
                                         .language(InvariantConstants.LANGUAGE_JAVASCRIPT)
                                         .expression("[\"" + String.join("\",\"", nodeRefs) + "\"]")
                                         .build());
            }
        }

        return invariants;
    }

    private Optional<String> resolveNodeRef(String node, boolean isAuthority) {
        if (NodeRef.isNodeRef(node)) {
            return Optional.of(node);
        } else if (isAuthority) {
            NodeRef nodeRef = authorityService.getAuthorityNodeRef(node);
            return Optional.ofNullable(nodeRef != null ? nodeRef.toString() : null);
        }
        return Optional.empty();
    }

    @Override
    public String getSupportedFieldType() {
        return TYPE;
    }

    @Override
    protected QName getDataType() {
        return DataTypeDefinition.NODE_REF;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.authorityService = serviceRegistry.getAuthorityService();
    }

    @Autowired
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }
}
