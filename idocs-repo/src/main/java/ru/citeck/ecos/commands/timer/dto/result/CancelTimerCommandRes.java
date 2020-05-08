package ru.citeck.ecos.commands.timer.dto.result;

import lombok.Data;

@Data
public class CancelTimerCommandRes {
    private boolean wasCancelled;
}