package ru.citeck.ecos.icase.commands;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.commands.providers.CaseCommandsProvider;

public interface CaseCommandsService {

    void executeCaseAction(ActivityRef actionActivityRef);

    void register(CaseCommandsProvider caseCommandsProvider);

}
