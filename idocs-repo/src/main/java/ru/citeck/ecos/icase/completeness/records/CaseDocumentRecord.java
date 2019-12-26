package ru.citeck.ecos.icase.completeness.records;

import ru.citeck.ecos.icase.completeness.dto.CaseDocumentDto;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;

public class CaseDocumentRecord implements MetaValue {

    private final CaseDocumentDto dto;

    public CaseDocumentRecord(CaseDocumentDto dto) {
        this.dto = dto;
    }

    @Override
    public String getDisplayName() {
        return dto.getType().getId();
    }

    @Override
    public String getId() {
        return dto.getType().getId();
    }

    @Override
    public Object getAttribute(String name, MetaField field) {
        switch (name) {
            case "type":
                return dto.getType();
            case "multiple":
                return dto.isMultiple();
            case "mandatory":
                return dto.isMandatory();
        }
        return null;
    }

    @Override
    public Object getJson() {
        return dto;
    }
}
