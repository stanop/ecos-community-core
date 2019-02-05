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
    private NamespacePrefixResolver prefixResolver;

    public JournalBatchEdit(BatchEdit batchEdit, String journalId, String attribute,
                            NamespacePrefixResolver prefixResolver, ServiceRegistry serviceRegistry) {

        this.prefixResolver = prefixResolver;
        dictionaryService = serviceRegistry.getDictionaryService();
        messageService = (MessageService) serviceRegistry.getService(AlfrescoServices.MESSAGE_SERVICE);

        attributeDefinition = getAttributeDefinition(attribute, dictionaryService);
        id = journalId + attribute.replaceAll(":", "_");
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
            if (attributeDefinition != null) {
                String attributeTitle = attributeDefinition.getTitle(messageService);
                return String.format(localizedTitle, attributeTitle);
            } else {
                return localizedTitle;
            }
        } else {
            return title;
        }
    }

    public String getId() {
        return id;
    }

    private ClassAttributeDefinition getAttributeDefinition(String attribute, DictionaryService dictionaryService) {

        if (!attribute.contains(":") && !attribute.contains("{")) {
            return null;
        }

        QName qname = QName.resolveToQName(prefixResolver, attribute);
        if (qname == null) {
            return null;
        }

        PropertyDefinition property = dictionaryService.getProperty(qname);
        if (property != null) {
            return property;
        } else {
            return dictionaryService.getAssociation(qname);
        }
    }
}
