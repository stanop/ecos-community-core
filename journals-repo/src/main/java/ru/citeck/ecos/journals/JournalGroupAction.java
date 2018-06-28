package ru.citeck.ecos.journals;

import org.alfresco.service.ServiceRegistry;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.journals.xml.GroupAction;
import ru.citeck.ecos.journals.xml.Option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class JournalGroupAction {

    private static final String VIEW_PARAM_NAME = "view";

    private String id;
    private JournalEvaluator evaluator;
    private Map<String, String> options;
    private String title;
    private String journalId;
    private String viewClass;

    public JournalGroupAction(GroupAction action, String journalId, ServiceRegistry serviceRegistry) {
        id    = action.getId();
        title = action.getTitle();

        options = new HashMap<>();

        for (Option option : action.getParam()) {
            options.put(option.getName(), option.getValue());
        }

        evaluator = new JournalEvaluator(action.getEvaluator(), journalId, serviceRegistry);

        viewClass = options.get(VIEW_PARAM_NAME);

        this.journalId = journalId;
    }

    /* Setters and Getters */

    public String getId() {
        return id;
    }

    public JournalEvaluator getEvaluator() {
        return evaluator;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getTitle() {
        String localizedTitle = I18NUtil.getMessage(title);

        return localizedTitle != null
                ? localizedTitle
                : title;
    }

    public String getJournalId() {
        return journalId;
    }

    public String getViewClass() {
        return viewClass;
    }
}
