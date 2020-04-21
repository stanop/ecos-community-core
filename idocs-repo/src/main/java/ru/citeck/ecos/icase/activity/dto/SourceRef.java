package ru.citeck.ecos.icase.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceRef {
    private String ref;
    private String scope;
}
