package ru.citeck.ecos.journals;

import lombok.Getter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.journals.xml.GroupAction;
import ru.citeck.ecos.journals.xml.Option;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class JournalGroupAction {

    @Getter private String id;
    @Getter private JournalEvaluator evaluator;
    @Getter private Map<String, String> options;
    private String title;

    public JournalGroupAction(GroupAction action, String journalId, ServiceRegistry serviceRegistry) {

        id = action.getId();
        options = new HashMap<>();
        title = action.getTitle();

        for (Option option : action.getParam()) {
            options.put(option.getName(), option.getValue());
        }
        evaluator = new JournalEvaluator(action.getEvaluator(), journalId, serviceRegistry);
    }

    public String getTitle() {
        String localizedTitle = I18NUtil.getMessage(title);
        return localizedTitle != null ? localizedTitle : title;
    }
}
