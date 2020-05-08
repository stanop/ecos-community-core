package ru.citeck.ecos.icase.completeness.records;

import lombok.Data;
import org.alfresco.service.namespace.QName;

@Data
public class CaseAssocToEcosType {
    private QName alfType;
    private QName assocName;
    private String ecosTypeRef;
}
