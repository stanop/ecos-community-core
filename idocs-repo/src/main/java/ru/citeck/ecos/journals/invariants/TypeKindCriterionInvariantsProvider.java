package ru.citeck.ecos.journals.invariants;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.model.ClassificationModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TypeKindCriterionInvariantsProvider extends CriterionInvariantsProvider {

    private final List<QName> SUPPORTED_TYPES = Arrays.asList(ClassificationModel.PROP_DOCUMENT_TYPE,
                                                              ClassificationModel.PROP_DOCUMENT_KIND);

    private final NodeRef TYPES_ROOT = new NodeRef("workspace://SpacesStore/category-document-type-root");

    @Override
    protected void beforeInitImpl() {
        setOrder(50);
    }

    @Override
    protected boolean isAttributeSupported(JournalType journalType, QName typeName, QName attribute) {
        return SUPPORTED_TYPES.contains(attribute);
    }

    @Override
    protected List<InvariantDefinition> getInvariantsImpl(JournalType journalType, QName typeName, QName attribute) {

        List<InvariantDefinition> invariants = new ArrayList<>();

        InvariantDefinition.Builder builder = new InvariantDefinition.Builder(namespaceService);
        builder.pushScope(attribute, InvariantScope.AttributeScopeKind.PROPERTY);

        if (ClassificationModel.PROP_DOCUMENT_TYPE.equals(attribute)) {

            invariants.add(builder
                    .feature(Feature.OPTIONS)
                    .explicit(getSubCategories(TYPES_ROOT))
                    .build());

        } else if (ClassificationModel.PROP_DOCUMENT_KIND.equals(attribute)) {

            PropertyDefinition typePropDef = dictUtils.getPropDef(typeName, ClassificationModel.PROP_DOCUMENT_TYPE);
            String defaultType = typePropDef.getDefaultValue();

            if (StringUtils.isNotBlank(defaultType) && NodeRef.isNodeRef(defaultType)) {

                invariants.add(builder
                        .feature(Feature.OPTIONS)
                        .explicit(getSubCategories(new NodeRef(defaultType)))
                        .build());
            }
        }

        return invariants;
    }

    private List<String> getSubCategories(NodeRef parent) {
        List<ChildAssociationRef> subCategories = nodeService.getChildAssocs(parent,
                                                                             ContentModel.ASSOC_SUBCATEGORIES,
                                                                             RegexQNamePattern.MATCH_ALL);
        return subCategories.stream()
                            .map(ref -> ref.getChildRef().toString())
                            .collect(Collectors.toList());
    }
}
