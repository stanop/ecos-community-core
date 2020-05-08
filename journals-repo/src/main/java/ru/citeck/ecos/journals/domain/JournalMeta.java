package ru.citeck.ecos.journals.domain;

import ecos.com.fasterxml.jackson210.databind.JsonNode;
import lombok.Data;
import ru.citeck.ecos.journals.webscripts.CreateVariantsGet;
import ru.citeck.ecos.records2.RecordRef;

import java.util.List;
import java.util.Map;

@Data
public class JournalMeta {
    private String nodeRef;
    private List<Criterion> criteria;
    private String title;
    private JsonNode predicate;
    private JsonNode groupBy;
    private String metaRecord;
    private List<CreateVariantsGet.ResponseVariant> createVariants;
    private List<RecordRef> actions;
    private List<GroupAction> groupActions;

    @Data
    public static class Criterion {
        String field;
        String predicate;
        String value;
    }

    @Data
    public static class GroupAction {
        String id;
        String title;
        Map<String, String> params;
        String type;
        String formKey;
    }
}


