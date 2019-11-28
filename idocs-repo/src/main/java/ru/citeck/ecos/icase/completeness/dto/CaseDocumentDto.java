package ru.citeck.ecos.icase.completeness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseDocumentDto {

    private String type;
    private boolean multiple;
    private boolean mandatory;

}
