package ru.citeck.ecos.icase.activity.service.eproc.importer.pojo;

import lombok.Data;
import ru.citeck.ecos.icase.activity.dto.SourceRef;

@Data
public class SentrySearchKey {
    private final SourceRef sourceRef;
    private final String eventType;
}
