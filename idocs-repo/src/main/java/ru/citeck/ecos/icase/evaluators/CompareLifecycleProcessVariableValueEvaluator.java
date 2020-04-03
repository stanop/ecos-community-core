package ru.citeck.ecos.icase.evaluators;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.lifecycle.LifeCycleServiceImpl;
import ru.citeck.ecos.records2.evaluator.RecordEvaluator;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorService;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class CompareLifecycleProcessVariableValueEvaluator implements
    RecordEvaluator<Object, Object, CompareLifecycleProcessVariableValueEvaluator.Config> {

    public static final String TYPE = "compare-process-variable";

    private RecordEvaluatorService recordEvaluatorService;

    @Autowired
    public CompareLifecycleProcessVariableValueEvaluator(RecordEvaluatorService recordEvaluatorService) {
        this.recordEvaluatorService = recordEvaluatorService;
    }

    @PostConstruct
    public void init() {
        recordEvaluatorService.register(this);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Object getMetaToRequest(Config config) {
        return null;
    }

    @Override
    public boolean evaluate(Object respMeta, Config config) {
        if (StringUtils.isBlank(config.variableName)) {
            log.error("Variable name is empty. Unavailable to compare variable");
            return false;
        }

        if (!AlfrescoTransactionSupport.isActualTransactionActive()) {
            log.error("Actual transaction is not active. Unable to compare variable " + config.variableName);
            return false;
        }

        Map<String, Object> processVariables = AlfrescoTransactionSupport.getResource(LifeCycleServiceImpl.PROCESS_VARS);
        if (processVariables == null) {
            log.error("Process variables are undefined. " +
                "Make sure you call this action condition evaluator in lifecycle context.");
            return false;
        }

        try {
            String currentValue = (String) processVariables.get(config.variableName);

            if ((currentValue == null) && (config.variableValue == null)) {
                return true;
            }

            return Objects.equals(currentValue, config.variableValue);
        } catch (ClassCastException e) {
            log.error("Unable to compare process variable. Only variables of type String are allowed for now.");
        }

        return false;
    }

    @Data
    public static class Config {
        private String variableName;
        private String variableValue;
    }
}
