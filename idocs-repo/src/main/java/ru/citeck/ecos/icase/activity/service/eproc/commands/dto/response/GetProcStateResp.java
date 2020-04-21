package ru.citeck.ecos.icase.activity.service.eproc.commands.dto.response;

import lombok.Data;

@Data
public class GetProcStateResp {
    private String procDefRevId;
    private byte[] stateData;
    private int version;
}