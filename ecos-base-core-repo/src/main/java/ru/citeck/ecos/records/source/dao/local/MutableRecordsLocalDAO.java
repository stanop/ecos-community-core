package ru.citeck.ecos.records.source.dao.local;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.source.dao.MutableRecordsDAO;

import java.util.List;

public interface MutableRecordsLocalDAO<T> extends MutableRecordsDAO {

    List<T> getValuesToMutate(List<RecordRef> records);

    RecordsMutResult save(List<T> values);
}
