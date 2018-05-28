package ru.citeck.ecos.flowable.form.view;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.flowable.form.model.FormField;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.invariants.view.NodeViewRegion;

import java.util.Map;
import java.util.Optional;

@Component
public class DecimalFieldConverter extends FieldConverter<FormField> {

    @Override
    protected Optional<NodeViewRegion> createInputRegion(FormField field, Map<String, Object> variables) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .template("number")
                                             .name("input")
                                             .build());
    }

    @Override
    public String getSupportedFieldType() {
        return "decimal";
    }

    @Override
    protected QName getDataType() {
        return DataTypeDefinition.DOUBLE;
    }
}
