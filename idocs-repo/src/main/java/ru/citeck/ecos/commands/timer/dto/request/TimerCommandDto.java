package ru.citeck.ecos.commands.timer.dto.request;

import lombok.Data;
import ru.citeck.ecos.commons.data.ObjectData;

@Data
public class TimerCommandDto {
    private String id;
    private String targetApp;
    private String type;
    private ObjectData body;
}
