package ru.citeck.ecos.graphql.journal.datasource.alfnode;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.journal.record.JournalAttributeInfoGql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlfNodeAttributeInfo implements JournalAttributeInfoGql {

    private List<String> defaultAttributes = Collections.emptyList();

    public AlfNodeAttributeInfo(QName name, DictionaryService dictionaryService) {
        PropertyDefinition propDef = dictionaryService.getProperty(name);
        if (propDef != null && propDef.getDataType().getName().equals(DataTypeDefinition.QNAME)) {
            defaultAttributes = new ArrayList<>();
            defaultAttributes.add("shortQName");
        }
    }

    @Override
    public List<String> getDefaultInnerAttributes() {
        return defaultAttributes;
    }
}
