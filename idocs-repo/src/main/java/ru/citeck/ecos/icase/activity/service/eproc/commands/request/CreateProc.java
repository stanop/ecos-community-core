package ru.citeck.ecos.icase.activity.service.eproc.commands.request;

import lombok.Data;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.records2.RecordRef;

@Data
@CommandType("create-proc-instance")
public class CreateProc {
    private String procDefRevId;
    private RecordRef recordRef;
}
