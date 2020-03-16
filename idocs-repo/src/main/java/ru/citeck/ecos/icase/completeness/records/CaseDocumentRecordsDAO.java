package ru.citeck.ecos.icase.completeness.records;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.completeness.CaseCompletenessService;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.meta.annotation.MetaAtt;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.request.result.RecordsResult;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.v2.LocalRecordsQueryWithMetaDAO;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CaseDocumentRecordsDAO extends LocalRecordsDAO implements LocalRecordsQueryWithMetaDAO {

    public final static String ID = "documents";
    private static final String DOCUMENT_TYPES_QUERY_LANGUAGE = "document-types";
    private static final String TYPES_DOCUMENTS_QUERY_LANGUAGE = "types-documents";
    private static final String DOCUMENTS_QUERY_LANGUAGE = "documents";

    private final CaseCompletenessService caseCompletenessService;
    private final SearchService searchService;

    @Autowired
    public CaseDocumentRecordsDAO(@Qualifier("caseCompletenessService")
                                      CaseCompletenessService caseCompletenessService,
                                  SearchService searchService) {
        setId(ID);
        this.caseCompletenessService = caseCompletenessService;
        this.searchService = searchService;
    }

    @Override
    public RecordsQueryResult<?> queryLocalRecords(RecordsQuery recordsQuery, MetaField field) {

        switch (recordsQuery.getLanguage()) {
            case DOCUMENT_TYPES_QUERY_LANGUAGE:
                return getDocumentTypes(recordsQuery);
            case TYPES_DOCUMENTS_QUERY_LANGUAGE:
                return getTypesDocuments(recordsQuery);
            case DOCUMENTS_QUERY_LANGUAGE:
                return getDocumentsOfAllTypes(recordsQuery);
            default:
                log.error("Language doesn't supported: " + recordsQuery.getLanguage());
        }

        return new RecordsQueryResult<>();
    }

    private RecordsQueryResult<TypeDocumentsRecord> getTypesDocuments(RecordsQuery recordsQuery) {

        TypesDocumentsQuery query = recordsQuery.getQuery(TypesDocumentsQuery.class);
        RecordRef recordRef = query.getRecordRef();
        List<RecordRef> typesRefs = query.getTypes();

        if (recordRef == null || !NodeRef.isNodeRef(recordRef.getId()) || typesRefs == null || typesRefs.isEmpty()) {
            return new RecordsQueryResult<>();
        }

        Map<RecordRef, List<DocInfo>> docsByType = getAllDocsByType(recordRef);

        List<TypeDocumentsRecord> typeDocumentsList = typesRefs.stream()
            .map(typeRef -> new TypeDocumentsRecord(typeRef, docsByType.getOrDefault(typeRef, Collections.emptyList())
                .stream()
                .map(DocInfo::getRef)
                .collect(Collectors.toList())))
            .collect(Collectors.toList());

        RecordsQueryResult<TypeDocumentsRecord> typeDocumentsRecords = new RecordsQueryResult<>();
        typeDocumentsRecords.setRecords(typeDocumentsList);
        return typeDocumentsRecords;
    }

    private RecordsQueryResult<TypeDocumentsRecord> getDocumentsOfAllTypes(RecordsQuery recordsQuery) {

        TypesDocumentsQuery query = recordsQuery.getQuery(TypesDocumentsQuery.class);
        RecordRef recordRef = query.getRecordRef();

        if (recordRef == null || !NodeRef.isNodeRef(recordRef.getId())) {
            return new RecordsQueryResult<>();
        }

        Map<RecordRef, List<DocInfo>> docsByType = getAllDocsByType(recordRef);

        List<TypeDocumentsRecord> documentsByTypes = docsByType.entrySet().stream()
            .map(e -> new TypeDocumentsRecord(e.getKey(), e.getValue().stream()
                .map(DocInfo::getRef)
                .collect(Collectors.toList())))
            .collect(Collectors.toList());

        RecordsQueryResult<TypeDocumentsRecord> documentsByTypesRecords = new RecordsQueryResult<>();
        documentsByTypesRecords.setRecords(documentsByTypes);
        return documentsByTypesRecords;
    }

    private Map<RecordRef, List<DocInfo>> getAllDocsByType(RecordRef documentRef) {

        FTSQuery ftsQuery = FTSQuery.createRaw()
            .parent(new NodeRef(documentRef.getId()))
            .transactional()
            .maxItems(1000);

        List<RecordRef> documentRefs = ftsQuery.query(searchService)
            .stream()
            .map(ref -> RecordRef.valueOf(ref.toString()))
            .collect(Collectors.toList());

        RecordsResult<DocumentTypeMeta> meta = recordsService.getMeta(documentRefs, DocumentTypeMeta.class);

        Map<RecordRef, List<DocInfo>> docsByType = new HashMap<>();

        for (int i = 0; i < meta.getRecords().size(); i++) {

            DocumentTypeMeta docMeta = meta.getRecords().get(i);

            if (docMeta.type != null) {

                long order = docMeta.getCreated() != null ? docMeta.getCreated().getTime() : 0L;

                DocInfo docInfo = new DocInfo(documentRefs.get(i), order);
                docsByType.computeIfAbsent(docMeta.getType(), t -> new ArrayList<>()).add(docInfo);
            }
        }

        docsByType.forEach((t, docs) -> docs.sort(Comparator.comparingLong(DocInfo::getOrder).reversed()));

        return docsByType;
    }

    private RecordsQueryResult<CaseDocumentRecord> getDocumentTypes(RecordsQuery recordsQuery) {

        RecordsQueryResult<CaseDocumentRecord> result = new RecordsQueryResult<>();

        DocumentTypesQuery queryData = recordsQuery.getQuery(DocumentTypesQuery.class);
        String recordRefStr = queryData.recordRef;

        RecordRef recordRef = RecordRef.valueOf(recordRefStr);

        if (!NodeRef.isNodeRef(recordRef.getId())) {
            log.warn("RecordRef id is not nodeRef");
            result.setRecords(Collections.emptyList());
            return result;
        }

        NodeRef nodeRef = new NodeRef(recordRef.getId());

        List<CaseDocumentRecord> documentRecords = caseCompletenessService.getCaseDocuments(nodeRef).stream()
            .map(CaseDocumentRecord::new)
            .collect(Collectors.toList());

        result.setRecords(documentRecords);

        return result;
    }

    @Data
    @RequiredArgsConstructor
    static class TypeDocumentsRecord implements MetaValue {

        private final String id = UUID.randomUUID().toString();

        private final RecordRef typeRef;
        private final List<RecordRef> documents;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Object getAttribute(String name, MetaField field) {

            switch (name) {
                case "type":
                    return typeRef.toString();
                case "documents":
                    return documents;
                case "docsCount":
                    return documents.size();
            }
            return null;
        }
    }

    @Data
    @AllArgsConstructor
    private static class DocInfo {
        private RecordRef ref;
        private long order;
    }

    @Data
    public static class DocumentTypeMeta {

        @MetaAtt("_etype?id")
        private RecordRef type;

        @MetaAtt("cm:created?str")
        private Date created;
    }

    @Data
    private static class TypesDocumentsQuery {

        private RecordRef recordRef;
        private List<RecordRef> types;
    }

    @Data
    private static class DocumentTypesQuery {

        private String recordRef;
    }
}
