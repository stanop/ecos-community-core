package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;

import java.util.ArrayList;
import java.util.List;

public class AlfNodeAttributeInfo implements JournalAttributeInfoGql {

    private String name;

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

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
        QName qname = QName.resolveToQName(namespaceService, name);
        PropertyDefinition propDef = dictionaryService.getProperty(qname);
        List<String> defaultAttributes = new ArrayList<>();
        if (propDef != null && propDef.getDataType().getName().equals(DataTypeDefinition.QNAME)) {
            defaultAttributes.add("shortName");
        }
        return defaultAttributes;
    }
}
