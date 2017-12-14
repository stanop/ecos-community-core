package ru.citeck.ecos.history;

import org.apache.commons.collections.CollectionUtils;
import ru.citeck.ecos.dto.HistoryEventTitlePairDto;
import ru.citeck.ecos.dto.HistoryEventTitlePairListDto;
import java.util.Collections;
import java.util.List;

/**
 * History event title mapper
 */
public class HistoryEventTitleMapper {

    /**
     * Title and object pairs
     */
    private List<HistoryEventTitlePairDto> titlePairs;

    /**
     * Complex title and object pairs
     */
    private List<HistoryEventTitlePairListDto> titleListPairs;

    /**
     * Getters and setters
     */

    public List<HistoryEventTitlePairDto> getTitlePairs() {
        return CollectionUtils.isNotEmpty(titlePairs) ? titlePairs : Collections.emptyList();
    }

    public void setTitlePairs(List<HistoryEventTitlePairDto> titlePairs) {
        this.titlePairs = titlePairs;
    }

    public List<HistoryEventTitlePairListDto> getTitleListPairs() {
        return CollectionUtils.isNotEmpty(titleListPairs) ? titleListPairs : Collections.emptyList();
    }

    public void setTitleListPairs(List<HistoryEventTitlePairListDto> titleListPairs) {
        this.titleListPairs = titleListPairs;
    }
}
