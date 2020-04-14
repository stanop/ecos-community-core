package ru.citeck.ecos.icase.completeness.records;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

public class CaseAssocTypeRegistrar {

    private CaseDocumentRecordsDAO caseDocumentRecordsDao;

    private List<CaseAssocToEcosType> assocs;
    private CaseAssocToEcosType assoc;

    @PostConstruct
    public void init() {
        if (assocs != null) {
            assocs.forEach(a -> caseDocumentRecordsDao.register(a));
        }
        if (assoc != null) {
            caseDocumentRecordsDao.register(assoc);
        }
    }

    public void setAssocs(List<CaseAssocToEcosType> assocs) {
        this.assocs = assocs;
    }

    public void setAssoc(CaseAssocToEcosType assoc) {
        this.assoc = assoc;
    }

    @Autowired
    public void setCaseDocumentRecordsDao(CaseDocumentRecordsDAO caseDocumentRecordsDao) {
        this.caseDocumentRecordsDao = caseDocumentRecordsDao;
    }
}
