package ru.citeck.ecos.icase.activity.service.eproc.commands.response;

import lombok.Data;

@Data
public class CreateProcResp {
    private String procId;
    private String procStateId;
    private byte[] procStateData;
}
