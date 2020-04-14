package ru.citeck.ecos.icase.activity.service.eproc.importer;

import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.records2.RecordRef;

import javax.annotation.PostConstruct;
import java.util.List;

public class EProcTypeRegistrar {

    private EProcCaseImporter eprocCaseImporter;
    private List<RecordRef> ecosTypes;
    private List<QName> alfTypes;

    @PostConstruct
    public void init() {
        if (CollectionUtils.isNotEmpty(ecosTypes)) {
            for (RecordRef ecosType : ecosTypes) {
                eprocCaseImporter.registerEcosType(ecosType);
            }
        }
        if (CollectionUtils.isNotEmpty(alfTypes)) {
            for (QName alfType : alfTypes) {
                eprocCaseImporter.registerAlfrescoType(alfType);
            }
        }
    }

    @Autowired
    public void setEprocCaseImporter(EProcCaseImporter eprocCaseImporter) {
        this.eprocCaseImporter = eprocCaseImporter;
    }

    public void setEcosTypes(List<RecordRef> ecosTypes) {
        this.ecosTypes = ecosTypes;
    }

    public void setAlfTypes(List<QName> alfTypes) {
        this.alfTypes = alfTypes;
    }
}
