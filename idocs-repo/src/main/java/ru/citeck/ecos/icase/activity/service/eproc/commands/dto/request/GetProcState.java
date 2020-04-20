package ru.citeck.ecos.icase.activity.service.eproc.commands.dto.request;

import lombok.Data;
import ru.citeck.ecos.commands.annotation.CommandType;

@Data
@CommandType("get-proc-state")
public class GetProcState {
    private String procType;
    private String procStateId;
}
