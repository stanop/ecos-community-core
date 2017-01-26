package ru.citeck.ecos.journals;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.journals.xml.BatchEdit;
import ru.citeck.ecos.journals.xml.Option;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class JournalBatchEdit {

    private static final String BATCH_EDIT_FUNCTION = "onBatchEdit";
    private static final String FUNCTION_KEY = "function";

    private String id;
    private JournalEvaluator evaluator;
    private Map<String, String> options;
    private String title = "journal.batch.edit.title";

    public JournalBatchEdit(BatchEdit batchEdit, String journalId, QName attribute,
                            NamespacePrefixResolver prefixResolver, ServiceRegistry serviceRegistry) {

        id = journalId + attribute.toPrefixString(prefixResolver).replaceAll(":", "_");
        evaluator = new JournalEvaluator(batchEdit.getEvaluator(), journalId, serviceRegistry);
        options = new HashMap<>();
        options.put(FUNCTION_KEY, BATCH_EDIT_FUNCTION);
        for (Option option : batchEdit.getOption()) {
            options.put(option.getName(), option.getValue());
        }
        if (batchEdit.getTitle() != null) {
            title = batchEdit.getTitle();
        }
    }

    public JournalEvaluator getEvaluator() {
        return evaluator;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getTitle() {
        String localizedTitle = I18NUtil.getMessage(title);
        return localizedTitle != null ? localizedTitle : title;
    }

    public String getId() {
        return id;
    }
}
