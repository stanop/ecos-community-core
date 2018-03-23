package ru.citeck.ecos.flowable.form.view;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.flowable.form.model.FormField;
import org.springframework.stereotype.Component;

@Component
public class TextFieldConverter extends FieldConverter<FormField> {

    @Override
    public String getSupportedFieldType() {
        return "text";
    }

    @Override
    protected QName getDataType() {
        return DataTypeDefinition.TEXT;
    }
}
