package ru.citeck.ecos.records.status;

import lombok.Data;
import ru.citeck.ecos.records2.RecordRef;

/**
 * @author Roman Makarskiy
 */
@Data
class StatusQuery {
    private RecordRef record;
    private String allExisting;
    private RecordRef allAvailableToChange;
}
