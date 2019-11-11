package ru.citeck.ecos.journals;

import lombok.Data;
import ru.citeck.ecos.journals.xml.ActionEvaluator;
import ru.citeck.ecos.journals.xml.Option;

import java.util.HashMap;
import java.util.Map;

@Data
public class JournalActionEvaluator {

    private String id;
    private Map<String, String> options;

    public JournalActionEvaluator(ActionEvaluator evaluator) {
        this.id = evaluator.getId();

        options = new HashMap<>();

        for (Option option : evaluator.getParam()) {
            options.put(option.getName(), option.getValue());
        }
    }
}
