package ru.citeck.ecos.journals;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class CreateVariantsResolverDto {

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private Map<String, String> params;

}
