package ru.citeck.ecos.history;

import java.util.List;
import java.util.Map;

/**
 * History remote service interface
 */
public interface HistoryRemoteService {

    /**
     * Get history records
     * @param documentUuid Document uuid
     * @return List of maps
     */
    List getHistoryRecords(String documentUuid);

    /**
     * Send history event to remote service
     * @param requestParams Request params
     */
    void sendHistoryEventToRemoteService(Map<String, Object> requestParams);

}
