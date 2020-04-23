package ru.citeck.ecos.journals.service;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.domain.JournalMeta;

import javax.annotation.Nullable;

public interface JournalMetaService {

    JournalMeta getJournalMeta(JournalType journal, @Nullable NodeRef journalNodeRef);
}
