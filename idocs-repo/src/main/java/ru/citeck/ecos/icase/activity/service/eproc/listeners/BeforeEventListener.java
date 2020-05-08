package ru.citeck.ecos.icase.activity.service.eproc.listeners;

import ru.citeck.ecos.icase.activity.dto.EventRef;

public interface BeforeEventListener extends OrderedListener {

    void beforeEvent(EventRef eventRef);

}
