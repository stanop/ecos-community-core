package ru.citeck.ecos.icase.commands.dto;

import lombok.Data;
import lombok.NonNull;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.SetPropertyValueCommandExecutor;
import ru.citeck.ecos.records2.RecordRef;

import java.io.Serializable;

@Data
@CommandType(SetPropertyValueCommandExecutor.TYPE)
public class SetPropertyValueCommand {
    @NonNull
    private final RecordRef caseRef;
    @NonNull
    private final QName propertyQName;
    private final Serializable propertyValue;
}
