package ru.citeck.ecos.records.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.records2.RecordRef;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CommandType("ecos.number.template.get-next")
public class GetNextNumberCommand {

    private RecordRef templateRef;
    private ObjectData model;
}
