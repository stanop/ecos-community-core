package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import lombok.Getter;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;

import java.util.ArrayList;
import java.util.List;

public class AlfNodeAttributeInfo implements JGqlAttributeInfo {

    private String name;

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    @Getter(lazy = true)
    private final QName qname = resolveQName();

    @Override
    public String name() {
        return name;
    }

    public AlfNodeAttributeInfo(String name, NamespaceService namespaceService, DictionaryService dictionaryService) {
        this.name = name;
        this.namespaceService = namespaceService;
        this.dictionaryService = dictionaryService;
    }

    @Override
    public List<String> getDefaultInnerAttributes() {
        List<String> defaultAttributes = new ArrayList<>();
        if (getDataType().equals(DataTypeDefinition.QNAME)) {
            defaultAttributes.add("shortName");
        }
        return defaultAttributes;
    }

    @Override
    public QName getDataType() {
        PropertyDefinition propDef = dictionaryService.getProperty(getQname());
        if (propDef != null) {
            return propDef.getDataType().getName();
        } else {
            AssociationDefinition assocDefinition = dictionaryService.getAssociation(getQname());
            if (assocDefinition != null) {
                return DataTypeDefinition.NODE_REF;
            }
        }
        return DataTypeDefinition.ANY;
    }

    private QName resolveQName() {
        return QName.resolveToQName(namespaceService, name);
    }
}
