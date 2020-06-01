package ru.citeck.ecos.records.source.alf;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.source.alf.meta.AlfNodeRecord;
import ru.citeck.ecos.records.source.alf.meta.DictRecord;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;
import ru.citeck.ecos.records2.source.dao.MutableRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsMetaLocalDAO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AlfDictionaryRecords extends LocalRecordsDAO
                                  implements RecordsMetaLocalDAO<MetaValue>,
                                             MutableRecordsDAO {

    public static final String ID = "dict";

    private final AlfNodesRecordsDAO alfNodesRecordsDAO;
    private NamespaceService namespaceService;

    @Autowired
    public AlfDictionaryRecords(AlfNodesRecordsDAO alfNodesRecordsDAO) {
        setId(ID);
        this.alfNodesRecordsDAO = alfNodesRecordsDAO;
    }

    @Override
    public List<MetaValue> getMetaValues(List<RecordRef> records) {

        return records.stream().map(r -> {
            QName typeName = QName.resolveToQName(namespaceService, r.getId());
            return new DictRecord(typeName, r.getId(), "alf_" + r.getId());

        }).collect(Collectors.toList());
    }

    @Override
    public RecordsMutResult mutateImpl(RecordsMutation mutation) {

        RecordsMutation alfNodesMut = new RecordsMutation(mutation, m -> {
            RecordMeta alfNodeMeta = new RecordMeta(m, id -> RecordRef.EMPTY);
            alfNodeMeta.setAttribute(AlfNodeRecord.ATTR_TYPE, m.getId().getId());
            return alfNodeMeta;
        });
        return alfNodesRecordsDAO.mutate(alfNodesMut);
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {
        return new RecordsDelResult();
    }

    @Autowired
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
}
