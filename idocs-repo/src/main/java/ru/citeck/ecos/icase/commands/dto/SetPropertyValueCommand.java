package ru.citeck.ecos.icase.commands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.icase.commands.executors.SetPropertyValueCommandExecutor;
import ru.citeck.ecos.records2.RecordRef;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CommandType(SetPropertyValueCommandExecutor.TYPE)
public class SetPropertyValueCommand {
    private RecordRef caseRef;
    private QName propertyQName;
    private Serializable propertyValue;
}
