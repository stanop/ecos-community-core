package ru.citeck.ecos.icase.activity.service.eproc.commands.dto.request;

import lombok.Data;
import ru.citeck.ecos.commands.annotation.CommandType;

@Data
@CommandType("get-proc-def-rev")
public class GetProcDefRev {
    private String procType;
    private String procDefRevId;
}
