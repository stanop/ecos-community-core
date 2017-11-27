package ru.citeck.ecos.history;

import ru.citeck.ecos.dto.HistoryEventTitlePairDto;
import ru.citeck.ecos.dto.HistoryEventTitlePairListDto;
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
        return titlePairs;
    }

    public void setTitlePairs(List<HistoryEventTitlePairDto> titlePairs) {
        this.titlePairs = titlePairs;
    }

    public List<HistoryEventTitlePairListDto> getTitleListPairs() {
        return titleListPairs;
    }

    public void setTitleListPairs(List<HistoryEventTitlePairListDto> titleListPairs) {
        this.titleListPairs = titleListPairs;
    }
}
