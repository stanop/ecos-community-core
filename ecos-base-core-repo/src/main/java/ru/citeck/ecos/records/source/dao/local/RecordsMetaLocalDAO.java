package ru.citeck.ecos.records.source.dao.local;

import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.source.dao.RecordsMetaDAO;

import java.util.List;

public interface RecordsMetaLocalDAO<T> extends RecordsMetaDAO {

    List<T> getMetaValues(List<RecordRef> records);
}
