package ru.citeck.ecos.icase.completeness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseDocumentDto {

    private String type;
    private boolean multiple;
    private boolean mandatory;

}
