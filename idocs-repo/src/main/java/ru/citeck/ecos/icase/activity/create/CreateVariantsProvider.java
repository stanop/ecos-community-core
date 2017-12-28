package ru.citeck.ecos.icase.activity.create;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.icase.activity.CaseActivityService;

import javax.annotation.PostConstruct;
import java.util.List;

public abstract class CreateVariantsProvider {

    protected static final String ACTIVITI_TASK_TITLE_PREFIX = "A: ";
    protected static final String FLOWABLE_TASK_TITLE_PREFIX = "F: ";
    protected static final String FLOWABLE_ENGINE_PREFIX = "flowable";

    private CaseActivityService caseActivityService;

    @PostConstruct
    public void init() {
        caseActivityService.registerCreateVariantsProvider(this);
    }

    public abstract List<ActivityCreateVariant> getCreateVariants();


    @Autowired
    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }
}

