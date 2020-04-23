package ru.citeck.ecos.journals.service;

import ru.citeck.ecos.journals.JournalType;
import ru.citeck.ecos.journals.domain.JournalTypeColumn;

import java.util.List;
import java.util.Set;

public interface JournalColumnService {

    List<JournalTypeColumn> getJournalTypeColumns(JournalType journalType, String metaRef);
}
