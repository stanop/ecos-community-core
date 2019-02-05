package ru.citeck.ecos.records.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.meta.value.MetaMapValue;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordConstants;
import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.delete.RecordsDelResult;
import ru.citeck.ecos.records.request.delete.RecordsDeletion;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;
import ru.citeck.ecos.records.source.alfnode.AlfNodesRecordsDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AlfDictionaryRecords extends LocalRecordsDAO
                                  implements RecordsMetaDAO,
                                             MutableRecordsDAO {

    public static final String ID = "dict";

    private AlfNodesRecordsDAO alfNodesRecordsDAO;

    @Autowired
    public AlfDictionaryRecords(AlfNodesRecordsDAO alfNodesRecordsDAO) {
        setId(ID);
        this.alfNodesRecordsDAO = alfNodesRecordsDAO;
    }

    @Override
    protected List<?> getMetaValues(List<RecordRef> records) {

        return (List) records.stream().map(r -> {

            Map<String, String> data = new HashMap<>();
            data.put(RecordConstants.ATT_FORM_KEY, "alf_" + r.getId());

            MetaMapValue value = new MetaMapValue(r.toString());
            value.setAttributes((Map) data);

            return (MetaValue) value;

        }).collect(Collectors.toList());
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {

        RecordsMutation alfNodesMut = new RecordsMutation(mutation, m -> {
            RecordMeta alfNodeMeta = new RecordMeta(m, id -> RecordRef.EMPTY);
            alfNodeMeta.setAttribute(RecordConstants.ATT_TYPE, m.getId().getId());
            return alfNodeMeta;
        });
        return alfNodesRecordsDAO.mutate(alfNodesMut);
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {
        return new RecordsDelResult();
    }
}
