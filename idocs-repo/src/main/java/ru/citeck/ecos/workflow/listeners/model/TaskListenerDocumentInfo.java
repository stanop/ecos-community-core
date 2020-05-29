package ru.citeck.ecos.workflow.listeners.model;

import lombok.Data;
import ru.citeck.ecos.records2.graphql.meta.annotation.MetaAtt;

@Data
public class TaskListenerDocumentInfo {
    @MetaAtt("type")
    private String documentType;

    @MetaAtt("icase:caseStatusAssoc.cm:name")
    private String statusName;

    @MetaAtt("icase:caseStatusAssoc.cm:title.ru")
    private String statusTitleRu;

    @MetaAtt("icase:caseStatusAssoc.cm:title.en")
    private String statusTitleEn;
}
