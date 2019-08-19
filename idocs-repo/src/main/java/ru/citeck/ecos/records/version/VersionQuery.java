package ru.citeck.ecos.records.version;

import lombok.Data;
import ru.citeck.ecos.records2.RecordRef;

/**
 * @author Roman Makarskiy
 */
@Data
class VersionQuery {
    private RecordRef record;
}
