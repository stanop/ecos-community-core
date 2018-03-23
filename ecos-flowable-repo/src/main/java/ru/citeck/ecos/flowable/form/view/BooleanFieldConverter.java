package ru.citeck.ecos.flowable.form.view;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.flowable.form.model.FormField;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.invariants.view.NodeViewRegion;

import java.util.*;

@Component
public class BooleanFieldConverter extends FieldConverter<FormField> {

    @Override
    protected Optional<NodeViewRegion> createInputRegion(FormField field) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .name("input")
                                             .template("checkbox")
                                             .build());
    }

    @Override
    protected QName getDataType() {
        return DataTypeDefinition.BOOLEAN;
    }

    @Override
    public String getSupportedFieldType() {
        return "boolean";
    }
}
