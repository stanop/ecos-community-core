package ru.citeck.ecos.icase.completeness.records.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.records2.RecordRef;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseDocumentsAssociation {

    private String childAssocQName;
    private RecordRef eTypeRef;

    public CaseDocumentsAssociation(String childAssocStr, String eTypeRefStr) {
        this.childAssocQName = childAssocStr;
        this.eTypeRef = RecordRef.valueOf(eTypeRefStr);
    }
}
