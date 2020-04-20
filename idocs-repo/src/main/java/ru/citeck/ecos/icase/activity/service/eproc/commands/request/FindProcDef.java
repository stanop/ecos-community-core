package ru.citeck.ecos.icase.activity.service.eproc.commands.request;

import lombok.Data;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.records2.RecordRef;

import java.util.List;

@Data
@CommandType("find-proc-def")
public class FindProcDef {
    private String procType;
    private RecordRef ecosTypeRef;
    private List<String> alfTypes;
}
