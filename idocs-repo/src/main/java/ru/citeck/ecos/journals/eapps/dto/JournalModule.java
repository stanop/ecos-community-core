package ru.citeck.ecos.journals.eapps.dto;

import lombok.Builder;
import lombok.Data;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.records2.RecordRef;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class JournalModule {

    // id = nodeRef_instance.getId()
    private String id;
    private String name;
    private RecordRef metaRecord;
    private ObjectData predicate;
    private List<RecordRef> actions;
    private String columnsJSONStr;
    private Map<String, String> attributes;
}
