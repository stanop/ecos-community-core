package ru.citeck.ecos.journals.domain;

import ecos.com.fasterxml.jackson210.databind.node.ObjectNode;
import lombok.Data;

@Data
public class JournalTypeColumnFormatter {

    private String name;
    private ObjectNode params;
}
