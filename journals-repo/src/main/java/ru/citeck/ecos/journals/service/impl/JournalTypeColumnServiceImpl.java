package ru.citeck.ecos.journals.service.impl;

import ecos.com.fasterxml.jackson210.databind.node.JsonNodeFactory;
import ecos.com.fasterxml.jackson210.databind.node.ObjectNode;
import ecos.com.fasterxml.jackson210.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.commons.data.DataValue;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.journals.JournalFormatter;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.domain.JournalMetaAttributeInfo;
import ru.citeck.ecos.journals.domain.JournalTypeColumn;
import ru.citeck.ecos.journals.domain.JournalTypeColumnFormatter;
import ru.citeck.ecos.journals.service.JournalColumnService;
import ru.citeck.ecos.records.source.alf.AlfDictionaryRecords;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalTypeColumnServiceImpl implements JournalColumnService {

    private final RecordsService recordsService;
    private MessageService messageService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;


    @Autowired
    public JournalTypeColumnServiceImpl(RecordsService recordsService,
                                        ServiceRegistry serviceRegistry) {
        this.recordsService = recordsService;
        this.messageService = serviceRegistry.getMessageService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.namespaceService = serviceRegistry.getNamespaceService();
    }

    @Override
    public List<JournalTypeColumn> getJournalTypeColumns(JournalType journalType, String metaRef) {

        List<JournalTypeColumn> columns = new ArrayList<>();
        List<String> attributes = journalType.getAttributes();
        Map<String, JournalMetaAttributeInfo> columnInfo = getAttributesInfo(metaRef, attributes);

        for (String name : attributes) {

            JournalTypeColumn column = new JournalTypeColumn();

            column.setFormatter(getFormatter(journalType.getFormatter(name)));
            column.setDefault(journalType.isAttributeDefault(name));
            column.setGroupable(journalType.isAttributeGroupable(name));
            column.setSearchable(journalType.isAttributeSearchable(name));
            column.setSortable(journalType.isAttributeSortable(name));
            column.setVisible(journalType.isAttributeVisible(name));
            column.setAttribute(name);
            column.setParams(journalType.getAttributeOptions(name));
            column.setText(getColumnLabel(column));

            if (column.getParams() != null) {
                String schema = column.getParams().get("schema");
                if (StringUtils.isNotBlank(schema)) {
                    column.setSchema(schema);
                }
            }

            JournalMetaAttributeInfo info = columnInfo.get(name);
            column.setJavaClass(info.getJavaClass() != null ? info.getJavaClass().getName() : null);
            column.setEditorKey(info.getEditorKey());
            column.setType(info.getType());

            columns.add(column);
        }

        return columns;
    }

    private Map<String, JournalMetaAttributeInfo> getAttributesInfo(String metaRecord, List<String> attributes) {

        Map<String, String> attributesEdges = new HashMap<>();
        for (String attribute : attributes) {
            attributesEdges.put(attribute, ".edge(n:\"" + attribute + "\"){type,editorKey,javaClass}");
        }

        RecordRef metaRecordRef;
        if (org.apache.commons.lang.StringUtils.isBlank(metaRecord)) {
            metaRecordRef = RecordRef.create(AlfDictionaryRecords.ID, metaRecord);
        } else {
            metaRecordRef = RecordRef.valueOf(metaRecord);
        }

        RecordMeta JournalMetaAttributeInfoMeta = recordsService.getAttributes(metaRecordRef, attributesEdges);

        Map<String, JournalMetaAttributeInfo> result = new HashMap<>();

        for (String attribute : attributes) {

            JournalMetaAttributeInfo info = null;

            DataValue JournalMetaAttributeInfoNode = JournalMetaAttributeInfoMeta.get(attribute);
            if (JournalMetaAttributeInfoNode.isObject()) {
                info = Json.getMapper().convert(JournalMetaAttributeInfoNode, JournalMetaAttributeInfo.class);
            }

            result.put(attribute, info != null ? info : new JournalMetaAttributeInfo());
        }

        return result;
    }

    private JournalTypeColumnFormatter getFormatter(JournalFormatter formatter) {

        if (formatter == null) {
            return null;
        }

        JournalTypeColumnFormatter result = new JournalTypeColumnFormatter();
        result.setName(formatter.getName());

        Map<String, String> journalParams = formatter.getParams();

        if (journalParams != null) {

            ObjectNode params = JsonNodeFactory.instance.objectNode();

            journalParams.forEach((k, v) -> {

                String value = v.trim();

                if (value.startsWith("{") || value.startsWith("[")) {
                    params.set(k, Json.getMapper().read(value));
                } else {
                    params.set(k, TextNode.valueOf(value));
                }
            });

            result.setParams(params);
        }

        return result;
    }

    private String getColumnLabel(JournalTypeColumn column) {

        Map<String, String> params = column.getParams();
        if (params != null) {
            String custom = params.get("customLabel");
            if (custom != null) {
                String label = I18NUtil.getMessage(custom);
                return label != null ? label : custom;
            }
        }

        if (column.getAttribute().contains(":")) {

            QName attQName = QName.resolveToQName(namespaceService, column.getAttribute());

            if (attQName != null) {

                ClassAttributeDefinition attDef = dictionaryService.getProperty(attQName);

                if (attDef == null) {
                    attDef = dictionaryService.getAssociation(attQName);
                }

                if (attDef != null) {

                    String title = attDef.getTitle(messageService);
                    if (org.apache.commons.lang.StringUtils.isNotBlank(title)) {
                        return title;
                    }
                }
            }
        }

        return column.getAttribute();
    }
}
