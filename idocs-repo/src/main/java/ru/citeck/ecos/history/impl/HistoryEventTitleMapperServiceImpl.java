package ru.citeck.ecos.history.impl;

import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.dto.HistoryEventTitlePairDto;
import ru.citeck.ecos.history.HistoryEventTitleMapper;
import ru.citeck.ecos.history.HistoryEventTitleMapperService;
import ru.citeck.ecos.providers.ApplicationContextProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * History event title mapper service implementation
 */
public class HistoryEventTitleMapperServiceImpl implements HistoryEventTitleMapperService {

    private Map<QName, QName> objectTitleMap;

    public void init() {
        objectTitleMap = new HashMap<>();
        Collection<HistoryEventTitleMapper> mappers = ApplicationContextProvider.getBeans(HistoryEventTitleMapper.class);
        if (mappers != null) {
            for (HistoryEventTitleMapper mapper : mappers) {
                if (mapper.getTitlePairs() != null) {
                    for (HistoryEventTitlePairDto dto : mapper.getTitlePairs()) {
                        objectTitleMap.put(dto.getObjectQName(), dto.getTitlePropertyQName());
                    }
                }
            }
        }
    }

    @Override
    public QName getTitleQName(QName objectQName) {
        return objectTitleMap.get(objectQName);
    }
}
