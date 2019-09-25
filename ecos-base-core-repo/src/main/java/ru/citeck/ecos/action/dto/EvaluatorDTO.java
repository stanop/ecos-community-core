package ru.citeck.ecos.action.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author Roman Makarskiy
 */
@Data
public class EvaluatorDTO {

    String id;
    Map<String, String> params;

}
