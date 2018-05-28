package ru.citeck.ecos.flowable.form.view;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.flowable.form.model.FormField;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.invariants.view.NodeViewRegion;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
public class FunctionalGroupFieldConverter extends FieldConverter<FormField> {

    @Override
    protected Optional<NodeViewRegion> createInputRegion(FormField field, Map<String, Object> variables) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .template("view")
                                             .name("input")
                                             .build());
    }

    @Override
    protected Optional<NodeViewRegion> createSelectRegion(FormField field, Map<String, Object> variables) {
        Map<String, Object> params = Collections.singletonMap("allowedAuthorityType", "GROUP");
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .template("select-orgstruct")
                                             .name("select")
                                             .templateParams(params)
                                             .build());
    }

    @Override
    public String getSupportedFieldType() {
        return "functional-group";
    }

    @Override
    protected QName getDataType() {
        return DataTypeDefinition.NODE_REF;
    }
}
