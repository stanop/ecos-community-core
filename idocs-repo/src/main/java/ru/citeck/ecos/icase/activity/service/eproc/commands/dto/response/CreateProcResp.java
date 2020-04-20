package ru.citeck.ecos.icase.activity.service.eproc.commands.dto.response;

import lombok.Data;

@Data
public class CreateProcResp {
    private String procId;
    private String procStateId;
    private byte[] procStateData;
}
