package ru.citeck.ecos.content.metadata;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.cmmn.CMMNUtils;
import ru.citeck.ecos.cmmn.model.Definitions;
import ru.citeck.ecos.content.converter.ContentValueConverter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CaseTemplateMetadataExtractor implements MetadataExtractor<Definitions> {

    private ContentValueConverter converter;

    @Override
    public Map<QName, Serializable> getMetadata(Definitions object) {

        Map<javax.xml.namespace.QName, String> attributes = object.getCase().get(0).getOtherAttributes();
        Map<QName, Serializable> result = new HashMap<>();

        for (Map.Entry<javax.xml.namespace.QName, QName> mapping : CMMNUtils.CASE_ATTRIBUTES_MAPPING.entrySet()) {
            String value = attributes.get(mapping.getKey());
            Serializable convertedValue = converter.convertToRepoValue(mapping.getValue(), value);
            if (convertedValue != null) {
                result.put(mapping.getValue(), convertedValue);
            }
        }

        return result;
    }

    public void setConverter(ContentValueConverter converter) {
        this.converter = converter;
    }
}
