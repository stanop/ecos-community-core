package ru.citeck.ecos.records.type;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NumTemplateDto {

    private String id;
    private String name;
    private String counterKey;
    private List<String> modelAttributes;
}
