package ru.citeck.ecos.icase.commands.providers;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.icase.commands.CaseCommandsService;

import javax.annotation.PostConstruct;

public abstract class AlfEprocCaseCommandsProvider implements CaseCommandsProvider {

    protected CaseCommandsService caseCommandsService;

    public AlfEprocCaseCommandsProvider(CaseCommandsService caseCommandsService) {
        this.caseCommandsService = caseCommandsService;
    }

    @PostConstruct
    public void init() {
        caseCommandsService.register(this);
    }

    @Override
    public Object provideCommandDto(ActivityRef activityRef) {
        if (activityRef.getCaseServiceType() == CaseServiceType.ALFRESCO) {
            return provideAlfrescoCommand(activityRef);
        } else {
            return provideEprocCommand(activityRef);
        }
    }

    protected abstract Object provideAlfrescoCommand(ActivityRef activityRef);

    protected abstract Object provideEprocCommand(ActivityRef activityRef);

}
