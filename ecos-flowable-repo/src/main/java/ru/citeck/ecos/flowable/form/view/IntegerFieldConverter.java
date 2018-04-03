package ru.citeck.ecos.flowable.form.view;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.flowable.form.model.FormField;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.invariants.view.NodeViewRegion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class IntegerFieldConverter extends FieldConverter<FormField> {

    @Override
    protected Optional<NodeViewRegion> createInputRegion(FormField field, Map<String, Object> variables) {

        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("step", "1");
        templateParams.put("isInteger", "true");

        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .template("number")
                                             .name("input")
                                             .templateParams(templateParams)
                                             .build());
    }

    @Override
    public String getSupportedFieldType() {
        return "integer";
    }

    @Override
    protected QName getDataType() {
        return DataTypeDefinition.INT;
    }
}
