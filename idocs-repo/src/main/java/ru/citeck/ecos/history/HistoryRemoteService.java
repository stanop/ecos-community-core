package ru.citeck.ecos.history;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;
import java.util.Map;

/**
 * History remote service interface
 */
public interface HistoryRemoteService {

    /**
     * Get history records
     *
     * @param documentUuid Document uuid
     * @return List of maps
     */
    List<Map> getHistoryRecords(String documentUuid);

    /**
     * Get history records by username
     *
     * @param username  Username
     * @return List of maps
     */
    List<Map> getHistoryRecordsByUsernameWithDateLimit(String username, Integer limit);

    /**
     * Get history records by username. Supports filtering by start date
     *
     * @param username  Username
     * @param startDate Start date. May be null or empty string
     * @return List of maps
     */
    List<Map> getHistoryRecordsByUsernameWithDateLimit(String username, String startDate, Integer limit);

    /**
     * Get history records by username. Supports filtering by start and end date
     *
     * @param username  Username
     * @param startDate Start date. May be null or empty string
     * @param endDate   End date. May be null or empty string
     * @return List of maps
     */
    List<Map> getHistoryRecordsByUsernameWithDateLimit(String username, String startDate, String endDate, Integer limit);

    /**
     * Send history event to remote service
     *
     * @param requestParams Request params
     */
    void sendHistoryEventToRemoteService(Map<String, Object> requestParams);

    /**
     * Send history events to remote service by document reference
     *
     * @param documentRef Document reference
     */
    void sendHistoryEventsByDocumentToRemoteService(NodeRef documentRef);

    /**
     * Send history event to remote service by event reference
     *
     * @param eventRef Event reference
     */
    void sendHistoryEventToRemoteService(NodeRef eventRef);

    /**
     * Update document history status
     *
     * @param documentNodeRef Document node reference
     * @param newStatus       New document status
     */
    void updateDocumentHistoryStatus(NodeRef documentNodeRef, boolean newStatus);

    /**
     * Remove history events by document
     *
     * @param documentNodeRef Document node reference
     */
    void removeEventsByDocument(NodeRef documentNodeRef);

}
