package ru.citeck.ecos.records.status;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.request.query.lang.DistinctQuery;
import ru.citeck.ecos.records2.source.common.group.DistinctValue;
import ru.citeck.ecos.search.SearchPredicate;
import ru.citeck.ecos.spring.registry.MappingRegistry;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Roman Makarskiy
 */
@Component
class StatusRecordsUtils {

    private static final String CONSTRAINT_STATUS_KEY = "listconstraint.idocs_constraint_documentStatus.";
    private static final String CRITERIA_LANGUAGE = "criteria";
    private static final String CONSTRAINT_ALLOWED_VALUES = "allowedValues";

    private static final String ATT_TYPE = "type";
    private static final String ATT_FIELD = "field_1";
    private static final String ATT_PREDICATE = "predicate_1";
    private static final String ATT_VALUE = "value_1";

    private final NodeService nodeService;
    private final RecordsService recordsService;
    private final CaseStatusService caseStatusService;
    private final MappingRegistry<String, String> typeToConstraintMapping;
    private final DictionaryService dictionaryService;
    private final NamespaceService namespaceService;

    @Autowired
    public StatusRecordsUtils(NodeService nodeService, RecordsService recordsService,
                              CaseStatusService caseStatusService,
                              @Qualifier("records.document-status.type-to-constraint.mappingRegistry")
                                      MappingRegistry<String, String> typeToConstraintMapping,
                              DictionaryService dictionaryService,
                              NamespaceService namespaceService) {
        this.nodeService = nodeService;
        this.recordsService = recordsService;
        this.caseStatusService = caseStatusService;
        this.typeToConstraintMapping = typeToConstraintMapping;
        this.dictionaryService = dictionaryService;
        this.namespaceService = namespaceService;
    }

    RecordsQueryResult<StatusRecord> getAllExistingStatuses(String type) {
        RecordsQueryResult<StatusRecord> existingCaseStatuses = getAllExistingCaseStatuses(type);
        if (existingCaseStatuses.getTotalCount() > 0) {
            return existingCaseStatuses;
        } else {
            return getAllExistingDocumentStatuses(type);
        }
    }

    private RecordsQueryResult<StatusRecord> getAllExistingCaseStatuses(String type) {
        RecordsQueryResult<StatusRecord> result = new RecordsQueryResult<>();

        RecordsQuery findAllAvailableQuery = new RecordsQuery();
        findAllAvailableQuery.setLanguage(DistinctQuery.LANGUAGE);

        DistinctQuery distinctQuery = new DistinctQuery();
        distinctQuery.setLanguage(CRITERIA_LANGUAGE);

        ObjectNode attributes = JsonNodeFactory.instance.objectNode();
        attributes.put(ATT_FIELD, ATT_TYPE);
        attributes.put(ATT_PREDICATE, SearchPredicate.TYPE_EQUALS.getValue());
        attributes.put(ATT_VALUE, type);

        distinctQuery.setQuery(attributes);
        distinctQuery.setAttribute(ICaseModel.ASSOC_CASE_STATUS.toPrefixString(namespaceService));

        findAllAvailableQuery.setQuery(distinctQuery);

        RecordsQueryResult<DistinctValue> values = recordsService.queryRecords(findAllAvailableQuery,
                DistinctValue.class);
        List<StatusRecord> statuses = values.getRecords()
                .stream()
                .map(value -> {
                    String ref = value.getValue();
                    return getByStatusRef(new NodeRef(ref));
                })
                .map(StatusRecord::new)
                .collect(Collectors.toList());

        result.setRecords(statuses);
        result.setTotalCount(statuses.size());

        return result;
    }

    private RecordsQueryResult<StatusRecord> getAllExistingDocumentStatuses(String type) {
        RecordsQueryResult<StatusRecord> result = new RecordsQueryResult<>();

        String constraintKey = typeToConstraintMapping.getMapping().get(type);
        if (StringUtils.isBlank(constraintKey)) {
            return result;
        }

        ConstraintDefinition statusConstraint = dictionaryService.getConstraint(QName.resolveToQName(namespaceService,
                constraintKey));
        Constraint constraint = statusConstraint.getConstraint();

        Map<String, Object> parameters = constraint.getParameters();

        // This cast is correct, because we know this is a ArrayList<String>
        @SuppressWarnings("unchecked")
        List<String> allowedValues = (List<String>) parameters.get(CONSTRAINT_ALLOWED_VALUES);

        List<StatusRecord> statuses = allowedValues.
                stream()
                .map(this::getByNameDocumentStatus)
                .map(StatusRecord::new)
                .collect(Collectors.toList());

        result.setRecords(statuses);

        return result;
    }

    RecordsQueryResult<StatusRecord> getAllAvailableToChangeStatuses(RecordRef recordRef) {
        if (recordRef == null || StringUtils.isBlank(recordRef.getId())) {
            throw new IllegalArgumentException("You mus specify a record to find comments");
        }

        String id = recordRef.getId();
        if (!NodeRef.isNodeRef(id)) {
            throw new IllegalArgumentException("Record id should be NodeRef format");
        }

        //TODO: implement
        return new RecordsQueryResult<>();
    }

    RecordsQueryResult<StatusRecord> getStatusByRecord(RecordRef recordRef) {
        if (recordRef == null || StringUtils.isBlank(recordRef.getId())) {
            throw new IllegalArgumentException("You mus specify a record to find comments");
        }

        String id = recordRef.getId();
        if (!NodeRef.isNodeRef(id)) {
            throw new IllegalArgumentException("Record id should be NodeRef format");
        }

        StatusDTO dto = getDocumentStatus(new NodeRef(id));

        if (dto != null) {

            StatusRecord statusRecord = new StatusRecord(dto);

            RecordsQueryResult<StatusRecord> result = new RecordsQueryResult<>();
            result.setRecords(Collections.singletonList(statusRecord));
            result.setTotalCount(1);
            return result;
        }
        return new RecordsQueryResult<>();
    }

    private StatusDTO getDocumentStatus(NodeRef document) {
        NodeRef statusRef = caseStatusService.getStatusRef(document);

        if (statusRef != null) {
            return getByStatusRef(statusRef);
        } else {
            Serializable documentStatus = nodeService.getProperty(document, IdocsModel.PROP_DOCUMENT_STATUS);
            if (documentStatus != null) {
                return getByNameCaseOrDocumentStatus((String) documentStatus);
            }
        }

        return null;
    }

    StatusDTO getByNameCaseOrDocumentStatus(String name) {
        NodeRef statusRef = caseStatusService.getStatusByName(name);
        if (statusRef == null) {
            return getByNameDocumentStatus(name);
        }

        return getByStatusRef(statusRef);
    }

    private StatusDTO getByNameDocumentStatus(String name) {
        StatusDTO dto = new StatusDTO();
        dto.setType(StatusType.DOCUMENT_STATUS.toString());
        dto.setId(name);

        String titleKey = CONSTRAINT_STATUS_KEY + name;
        dto.setName(I18NUtil.getMessage(titleKey));

        return dto;
    }

    private StatusDTO getByStatusRef(NodeRef statusRef) {
        StatusDTO dto = new StatusDTO();

        Map<QName, Serializable> properties = nodeService.getProperties(statusRef);
        String name = (String) properties.get(ContentModel.PROP_NAME);

        Serializable title = properties.get(ContentModel.PROP_TITLE);
        if (title == null) {
            title = name;
        }

        dto.setName((String) title);
        dto.setType(StatusType.CASE_STATUS.toString());
        dto.setId(name);

        return dto;
    }

}
