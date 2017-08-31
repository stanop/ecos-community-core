package ru.citeck.ecos.history;

import org.alfresco.service.namespace.QName;

/**
 * History event title mapper service
 */
public interface HistoryEventTitleMapperService {

    QName getTitleQName(QName objectQName);
}
