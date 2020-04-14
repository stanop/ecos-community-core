package ru.citeck.ecos.icase.activity.service.eproc.listeners;

import ru.citeck.ecos.icase.activity.dto.EventRef;

public interface OnEventListener extends OrderedListener {

    void onEvent(EventRef eventRef);

}
