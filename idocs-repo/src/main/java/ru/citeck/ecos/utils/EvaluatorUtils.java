package ru.citeck.ecos.utils;

import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.records2.evaluator.RecordEvaluatorDto;

public class EvaluatorUtils {

    public static RecordEvaluatorDto createEvaluatorDto(String type, Object config, boolean inverse) {
        RecordEvaluatorDto evaluatorDto = new RecordEvaluatorDto();
        evaluatorDto.setType(type);
        evaluatorDto.setInverse(inverse);
        ObjectData objectData = new ObjectData(config);
        evaluatorDto.setConfig(objectData);
        return evaluatorDto;
    }

}
