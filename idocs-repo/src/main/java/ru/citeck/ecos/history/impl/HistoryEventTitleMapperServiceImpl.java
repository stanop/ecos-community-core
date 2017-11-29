package ru.citeck.ecos.history.impl;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.dto.HistoryEventTitlePairDto;
import ru.citeck.ecos.dto.HistoryEventTitlePairListDto;
import ru.citeck.ecos.history.HistoryEventTitleMapper;
import ru.citeck.ecos.history.HistoryEventTitleMapperService;
import ru.citeck.ecos.providers.ApplicationContextProvider;

import java.util.*;

/**
 * History event title mapper service implementation
 */
public class HistoryEventTitleMapperServiceImpl implements HistoryEventTitleMapperService {

    /**
     * Object title - object type map
     */
    private Map<QName, QName> objectTitleMap;

    /**
     * Object titles - object type map
     */
    private Map<QName, List<QName>> objectTitleListMap;

    /**
     * Init
     */
    public void init() {
        objectTitleMap = new HashMap<>();
        objectTitleListMap = new HashMap<>();
        Collection<HistoryEventTitleMapper> mappers = ApplicationContextProvider.getBeans(HistoryEventTitleMapper.class);
        if (mappers != null) {
            for (HistoryEventTitleMapper mapper : mappers) {
                /** Simple title-object pairs */
                if (mapper.getTitlePairs() != null) {
                    for (HistoryEventTitlePairDto dto : mapper.getTitlePairs()) {
                        objectTitleMap.put(dto.getObjectQName(), dto.getTitlePropertyQName());
                    }
                }
                /** Complex title-object pairs */
                if (mapper.getTitleListPairs() != null) {
                    for (HistoryEventTitlePairListDto dto : mapper.getTitleListPairs()) {
                        objectTitleListMap.put(dto.getObjectQName(), dto.getTitles());
                    }
                }
            }
        }
    }

    /**
     * Get title qname by object type
     * @param objectQName Object type
     * @return Title qname
     */
    @Override
    public QName getTitleQName(QName objectQName) {
        return objectTitleMap.get(objectQName);
    }

    /**
     * Get titles qnames for complex title building by object type
     * @param objectQName Object type
     * @return Titles qnames
     */
    @Override
    public List<QName> getTitleQNames(QName objectQName) {
        return objectTitleListMap.containsKey(objectQName) ? objectTitleListMap.get(objectQName) : Collections.emptyList();
    }
}
