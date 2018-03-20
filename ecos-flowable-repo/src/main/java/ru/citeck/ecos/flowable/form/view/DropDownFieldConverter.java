package ru.citeck.ecos.flowable.form.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.flowable.form.model.Option;
import org.flowable.form.model.OptionFormField;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.invariants.*;
import ru.citeck.ecos.invariants.view.NodeViewRegion;

import java.util.*;

@Component
public class DropDownFieldConverter extends FieldConverter<OptionFormField> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected Optional<NodeViewRegion> createInputRegion(OptionFormField field) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .name("input")
                                             .template("select")
                                             .build());
    }

    @Override
    protected List<InvariantDefinition> getInvariants(OptionFormField field, QName fieldName) {

        List<InvariantDefinition> invariants = super.getInvariants(field, fieldName);

        List<String> options = new ArrayList<>();
        Map<String, String> optionsTitle = new HashMap<>();

        for (Option option : field.getOptions()) {
            String id = option.getId() != null ? option.getId() : option.getName();
            optionsTitle.put(id, option.getName());
        }

        InvariantDefinition invDef;

        invDef = new InvariantDefinition.Builder(prefixResolver)
                                        .pushScope(fieldName, InvariantScope.AttributeScopeKind.PROPERTY)
                                        .feature(Feature.OPTIONS)
                                        .explicit(options)
                                        .priority(InvariantPriority.COMMON)
                                        .build();
        invariants.add(invDef);

        String valueTitleExpression = "(function(){var opts = %s; return opts[value] || value;})()";
        try {
            valueTitleExpression = String.format(valueTitleExpression, mapper.writeValueAsString(optionsTitle));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            valueTitleExpression = String.format(valueTitleExpression, "{}");
        }

        invDef = new InvariantDefinition.Builder(prefixResolver)
                                        .pushScope(fieldName, InvariantScope.AttributeScopeKind.PROPERTY)
                                        .feature(Feature.VALUE_TITLE)
                                        .language(InvariantConstants.LANGUAGE_JAVASCRIPT)
                                        .expression(valueTitleExpression)
                                        .build();
        invariants.add(invDef);

        return invariants;
    }

    @Override
    public String getSupportedFieldType() {
        return "dropdown";
    }

    @Override
    protected QName getDataType() {
        return DataTypeDefinition.TEXT;
    }
}
