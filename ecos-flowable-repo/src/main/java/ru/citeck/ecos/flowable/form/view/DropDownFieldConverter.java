package ru.citeck.ecos.flowable.form.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.flowable.form.model.Option;
import org.flowable.form.model.OptionFormField;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.invariants.*;
import ru.citeck.ecos.invariants.view.NodeViewRegion;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DropDownFieldConverter extends FieldConverter<OptionFormField> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected Optional<NodeViewRegion> createInputRegion(OptionFormField field, Map<String, Object> variables) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .name("input")
                                             .template("select")
                                             .build());
    }

    @Override
    protected List<InvariantDefinition> getInvariants(OptionFormField field,
                                                      QName fieldName,
                                                      Object defaultValue,
                                                      Map<String, Object> variables) {

        List<InvariantDefinition> invariants = super.getInvariants(field,
                                                                   fieldName,
                                                                   defaultValue,
                                                                   variables);

        List<String> options = new ArrayList<>();
        Map<String, String> optionsTitle = new HashMap<>();

        for (Option option : field.getOptions()) {
            fillOptions(option, options, optionsTitle);
        }
        if (CollectionUtils.isEmpty(options)) {
            String optionsExpression = field.getOptionsExpression();
            if (StringUtils.isNotBlank(optionsExpression)) {
                String optionsExpressionVariable;
                Pattern pattern = Pattern.compile("^\\$\\{(\\w+)}$");
                Matcher matcher = pattern.matcher(optionsExpression);
                if (matcher.find()) {
                    optionsExpressionVariable = matcher.group(1);
                    if (StringUtils.isNotBlank(optionsExpressionVariable)) {
                        if (variables.containsKey(optionsExpressionVariable)) {
                            Object optionsExpressionValue = variables.get(optionsExpressionVariable);
                            if (optionsExpressionValue instanceof List) {
                                for (Object option : (List) optionsExpressionValue) {
                                    if (option instanceof Option) {
                                        fillOptions((Option) option, options, optionsTitle);
                                        continue;
                                    }
                                    options.add((String) option);
                                }
                            }
                        }
                    }
                }
            }
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

    private void fillOptions(Option option, List<String> options, Map<String, String> optionsTitle) {
        String id = option.getId() == null ? option.getName() : option.getId();
        options.add(id);
        optionsTitle.put(id, option.getName());
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
