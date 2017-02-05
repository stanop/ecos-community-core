package ru.citeck.ecos.journals;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.journals.xml.BatchEdit;
import ru.citeck.ecos.journals.xml.Option;
import ru.citeck.ecos.service.AlfrescoServices;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class JournalBatchEdit {

    private String id;
    private JournalEvaluator evaluator;
    private Map<String, String> options;
    private String title = "journal.batch.edit.title";
    private ClassAttributeDefinition attributeDefinition;

    private DictionaryService dictionaryService;
    private MessageService messageService;

    public JournalBatchEdit(BatchEdit batchEdit, String journalId, QName attribute,
                            NamespacePrefixResolver prefixResolver, ServiceRegistry serviceRegistry) {

        dictionaryService = serviceRegistry.getDictionaryService();
        messageService = (MessageService) serviceRegistry.getService(AlfrescoServices.MESSAGE_SERVICE);

        attributeDefinition = getAttributeDefinition(attribute, dictionaryService);
        id = journalId + attribute.toPrefixString(prefixResolver).replaceAll(":", "_");
        evaluator = new JournalEvaluator(batchEdit.getEvaluator(), journalId, serviceRegistry);
        options = new HashMap<>();
        for (Option option : batchEdit.getParam()) {
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
        if (localizedTitle != null) {
            String attributeTitle = attributeDefinition.getTitle(messageService);
            return String.format(localizedTitle, attributeTitle);
        } else {
            return title;
        }
    }

    public String getId() {
        return id;
    }

    private ClassAttributeDefinition getAttributeDefinition(QName attribute, DictionaryService dictionaryService) {
        PropertyDefinition property = dictionaryService.getProperty(attribute);
        if (property != null) {
            return property;
        } else {
            return dictionaryService.getAssociation(attribute);
        }
    }
}
