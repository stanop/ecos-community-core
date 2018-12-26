package ru.citeck.ecos.records.source;

import ru.citeck.ecos.records.request.delete.RecordsDelResult;
import ru.citeck.ecos.records.request.delete.RecordsDeletion;
import ru.citeck.ecos.records.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records.request.mutation.RecordsMutation;

public interface MutableRecordsDAO {

    RecordsMutResult mutate(RecordsMutation mutation);

    RecordsDelResult delete(RecordsDeletion deletion);
}
