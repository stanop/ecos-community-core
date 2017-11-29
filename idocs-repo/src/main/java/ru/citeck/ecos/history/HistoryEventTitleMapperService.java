package ru.citeck.ecos.history;

import org.alfresco.service.namespace.QName;
import java.util.List;

/**
 * History event title mapper service
 */
public interface HistoryEventTitleMapperService {

    /**
     * Get title qname by object type
     * @param objectQName Object type
     * @return Title qname
     */
    QName getTitleQName(QName objectQName);

    /**
     * Get titles qnames for complex title building by object type
     * @param objectQName Object type
     * @return Titles qnames
     */
    List<QName> getTitleQNames(QName objectQName);
}
