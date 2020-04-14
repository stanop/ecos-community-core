package ru.citeck.ecos.icase.activity.service.eproc.commands.response;

import lombok.Data;

@Data
public class GetProcDefRevResp {
    private String id;
    private String format;
    private byte[] data;
    private String procDefId;
    private int version;
}