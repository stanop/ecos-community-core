package ru.citeck.ecos.journals.service;

import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.domain.JournalTypeColumn;

import java.util.Set;

public interface JournalColumnService {

    Set<JournalTypeColumn> getJournalTypeColumns(JournalType journalType, String metaRef);
}
