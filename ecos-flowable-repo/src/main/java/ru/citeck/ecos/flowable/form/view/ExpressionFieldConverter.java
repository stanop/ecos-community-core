package ru.citeck.ecos.flowable.form.view;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.common.api.variable.VariableContainer;
import org.flowable.engine.common.impl.el.ExpressionManager;
import org.flowable.engine.common.impl.el.VariableContainerWrapper;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.form.model.ExpressionFormField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.invariants.view.NodeViewRegion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Pavel Simonov
 */
@Component
public class ExpressionFieldConverter extends FieldConverter<ExpressionFormField> {

    private static final Log logger = LogFactory.getLog(ExpressionFieldConverter.class);

    @Autowired
    @Qualifier("flowableEngineConfiguration")
    private ProcessEngineConfigurationImpl flowableEngineConfiguration;

    @Override
    protected Optional<NodeViewRegion> createInputRegion(ExpressionFormField field, Map<String, Object> variables) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .name("input")
                                             .template("text")
                                             .build());
    }

    @Override
    protected Optional<NodeViewRegion> createLabelRegion(ExpressionFormField field, Map<String, Object> variables) {
        return Optional.empty();
    }

    @Override
    protected Optional<NodeViewRegion> createMandatoryRegion(ExpressionFormField field, Map<String, Object> variables) {
        return Optional.empty();
    }


    @Override
    protected List<InvariantDefinition> getInvariants(ExpressionFormField field,
                                                      QName fieldName,
                                                      Object defaultValue,
                                                      Map<String, Object> variables) {

        List<InvariantDefinition> invariants = super.getInvariants(field, fieldName, null, variables);

        InvariantDefinition.Builder invBuilder = new InvariantDefinition.Builder(prefixResolver);
        invBuilder.pushScope(fieldName, InvariantScope.AttributeScopeKind.PROPERTY);

        ExpressionManager expressionManager = flowableEngineConfiguration.getExpressionManager();

        if (expressionManager != null) {
            Object value;
            try {
                VariableContainer varsContainer = new VariableContainerWrapper(variables);

                varsContainer.setVariable("execution", varsContainer);
                varsContainer.setVariable("task", varsContainer);

                value = expressionManager.createExpression(field.getExpression())
                                         .getValue(varsContainer);
            } catch (Exception e) {
                value = "Error";
                logger.error("Expression error", e);
            }
            invariants.add(invBuilder.feature(Feature.VALUE)
                                     .explicit(value)
                                     .build());
        } else {
            logger.warn("ExpressionManager is null");
        }

        return invariants;
    }

    @Override
    public String getSupportedFieldType() {
        return "expression";
    }

    @Override
    protected QName getDataType() {
        return DataTypeDefinition.TEXT;
    }
}
