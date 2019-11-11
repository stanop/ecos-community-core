package ru.citeck.ecos.journals;

import lombok.Data;
import ru.citeck.ecos.journals.xml.Action;
import ru.citeck.ecos.journals.xml.Option;

import java.util.HashMap;
import java.util.Map;

@Data
public class JournalAction {

    private String id;
    private Map<String, String> options;
    private String title;
    private String type;
    private JournalActionEvaluator evaluator;

    public JournalAction(Action action) {
        this.id = action.getId();
        this.title = action.getTitle();
        this.type = action.getType();

        if (action.getEvaluator() != null) {
            this.evaluator = new JournalActionEvaluator(action.getEvaluator());
        }

        this.options = new HashMap<>();

        for (Option option : action.getParam()) {
            this.options.put(option.getName(), option.getValue());
        }
    }
}
