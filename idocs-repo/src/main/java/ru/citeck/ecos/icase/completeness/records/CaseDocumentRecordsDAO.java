package ru.citeck.ecos.icase.completeness.records;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.completeness.CaseCompletenessService;
import ru.citeck.ecos.icase.completeness.dto.CaseDocumentDto;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.v2.LocalRecordsMetaDAO;
import ru.citeck.ecos.records2.source.dao.local.v2.LocalRecordsQueryWithMetaDAO;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CaseDocumentRecordsDAO extends LocalRecordsDAO
    implements LocalRecordsQueryWithMetaDAO<CaseDocumentRecord>, LocalRecordsMetaDAO<CaseDocumentRecord> {

    public final static String ID = "documents";
    private static final String DOCUMENT_TYPES_QUERY_LANGUAGE = "document-types";
    private final static CaseDocumentRecord EMPTY_RECORD = new CaseDocumentRecord(new CaseDocumentDto());

    private final CaseCompletenessService caseCompletenessService;

    @Autowired
    public CaseDocumentRecordsDAO(@Qualifier("caseCompletenessService")
                                      CaseCompletenessService caseCompletenessService) {
        setId(ID);
        this.caseCompletenessService = caseCompletenessService;
    }

    @Override
    public RecordsQueryResult<CaseDocumentRecord> queryLocalRecords(RecordsQuery recordsQuery, MetaField field) {

        RecordsQueryResult<CaseDocumentRecord> result = new RecordsQueryResult<>();

        if (recordsQuery.getLanguage().equals(DOCUMENT_TYPES_QUERY_LANGUAGE)) {

            DocumentTypesQuery queryData = recordsQuery.getQuery(DocumentTypesQuery.class);
            String recordRefStr = queryData.recordRef;

            RecordRef recordRef = RecordRef.valueOf(recordRefStr);

            if (!NodeRef.isNodeRef(recordRef.getId())) {
                throw new IllegalArgumentException("RecordRef id is not nodeRef");
            }

            NodeRef nodeRef = new NodeRef(recordRef.getId());

            List<CaseDocumentRecord> documentRecords = caseCompletenessService.getCaseDocuments(nodeRef).stream()
                .map(CaseDocumentRecord::new)
                .collect(Collectors.toList());

            result.setRecords(documentRecords);
        }

        return result;
    }

    @Override
    public List<CaseDocumentRecord> getLocalRecordsMeta(List<RecordRef> records, MetaField metaField) {

        if (records.size() == 1 && records.get(0).getId().isEmpty()) {
            return Collections.singletonList(EMPTY_RECORD);
        }

        Set<String> requestedIDs = records.stream()
            .map(RecordRef::getId)
            .collect(Collectors.toSet());

        Set<NodeRef> nodeRefs = requestedIDs.stream()
            .map(NodeRef::new)
            .collect(Collectors.toSet());

        return nodeRefs.stream()
            .flatMap(nodeRef -> {

                Set<CaseDocumentDto> documentDtos = caseCompletenessService.getCaseDocuments(nodeRef);

                return documentDtos.stream()
                    .map(CaseDocumentRecord::new);
            })
            .collect(Collectors.toList());
    }

    private static class DocumentTypesQuery {

        public String recordRef;
    }
}
