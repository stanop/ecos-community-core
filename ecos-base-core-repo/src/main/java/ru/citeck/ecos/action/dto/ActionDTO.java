package ru.citeck.ecos.action.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author Roman Makarskiy
 */
@Data
public class ActionDTO {

    String id;
    String title;
    String type;
    EvaluatorDTO evaluator;
    Map<String, String> params;

}
