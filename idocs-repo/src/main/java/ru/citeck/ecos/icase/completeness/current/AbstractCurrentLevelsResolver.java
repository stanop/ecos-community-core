package ru.citeck.ecos.icase.completeness.current;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.icase.completeness.CaseCompletenessService;

import javax.annotation.PostConstruct;

public abstract class AbstractCurrentLevelsResolver implements CurrentLevelsResolver {

    protected CaseCompletenessService caseCompletenessService;

    @PostConstruct
    public void registerResolver() {
        caseCompletenessService.register(this);
    }

    @Autowired
    @Qualifier("caseCompletenessService")
    public void setCaseCompletenessService(CaseCompletenessService caseCompletenessService) {
        this.caseCompletenessService = caseCompletenessService;
    }
}
