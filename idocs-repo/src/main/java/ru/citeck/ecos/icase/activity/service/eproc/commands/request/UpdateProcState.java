package ru.citeck.ecos.icase.activity.service.eproc.commands.request;

import lombok.Data;
import ru.citeck.ecos.commands.annotation.CommandType;

@Data
@CommandType("update-proc-state")
public class UpdateProcState {
    private String prevProcStateId;
    private byte[] stateData;
}
