package ru.citeck.ecos.icase.activity.service.eproc;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventDelegate;

@Component
public class EProcCaseActivityEventDelegate implements CaseActivityEventDelegate {

    @Override
    public void fireEvent(ActivityRef activityRef, String eventType) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void fireConcreteEvent(EventRef eventRef) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public boolean checkConditions(EventRef eventRef) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
