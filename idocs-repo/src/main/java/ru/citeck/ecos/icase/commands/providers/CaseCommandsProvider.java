package ru.citeck.ecos.icase.commands.providers;

import ru.citeck.ecos.icase.activity.dto.ActivityRef;

public interface CaseCommandsProvider {

    String getType();

    Object provideCommandDto(ActivityRef activityRef);

}
