package ru.citeck.ecos.icase.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluatorDefinitionDataHolder {
    private List<EvaluatorDefinitionData> data;
}
