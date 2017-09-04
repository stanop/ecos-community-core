package ru.citeck.ecos.history;

import ru.citeck.ecos.dto.HistoryEventTitlePairDto;
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
     * Getters and setters
     */

    public List<HistoryEventTitlePairDto> getTitlePairs() {
        return titlePairs;
    }

    public void setTitlePairs(List<HistoryEventTitlePairDto> titlePairs) {
        this.titlePairs = titlePairs;
    }
}
