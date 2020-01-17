package ru.citeck.ecos.icase.completeness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.citeck.ecos.records2.RecordRef;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseDocumentDto {

    private RecordRef type;
    private boolean multiple;
    private boolean mandatory;

}
