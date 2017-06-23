package ru.citeck.ecos.history.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.history.HistoryRemoteService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * History remote service
 */
public class HistoryRemoteServiceImpl implements HistoryRemoteService {

    /**
     * Constants
     */
    private static final String[] KEYS= {
            "historyEventId", "documentId", "eventType", "comments", "version", "creationTime", "username", "userId"
    };
    private static final String HISTORY_RECORD_FILE_NAME = "history_record";
    private static final String DELIMETER = ";";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    /**
     * Logger
     */
    private static Log logger = LogFactory.getLog(HistoryRemoteServiceImpl.class);

    /**
     * Properties values
     */
    @Value("${ecos.citek.history.service.host}" )
    private String historyServiceHost;
    @Value("${ecos.citek.history.service.insert.record.path}")
    private String insertRecordPath;
    @Value("${ecos.citek.history.service.get.records.path}")
    private String getByDocumentRecordsPath;
    @Value("${ecos.citek.history.service.csv.folder}")
    private String csvFolder;

    /**
     * Services
     */
    private RestTemplate restTemplate;

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get history records
     * @param documentUuid Document uuid
     * @return List of maps
     */
    @Override
    public List getHistoryRecords(String documentUuid) {
        return restTemplate.getForObject(historyServiceHost + getByDocumentRecordsPath + documentUuid, List.class);
    }

    /**
     * Send history event to remote service
     * @param requestParams Request params
     */
    @Override
    public void sendHistoryEventToRemoteService(Map<String, Object> requestParams) {
        try {
            restTemplate.postForObject(historyServiceHost + insertRecordPath, requestParams, String.class);
        } catch (Exception exception) {
            logger.error(exception);
            saveHistoryRecordAsCsv(requestParams);
        }
    }

    /**
     * Save history record as csv file
     * @param requestParams Request params
     */
    private void saveHistoryRecordAsCsv(Map<String, Object> requestParams) {
        /** Make csv string */
        StringBuilder csvResult = new StringBuilder();
        for (String key : KEYS) {
            Object value = requestParams.get(key);
            csvResult.append((value != null ? value.toString() : "") + DELIMETER);
        }
        /** Create file */
        String currentDate = dateFormat.format(new Date());
        File csvFile = new File(csvFolder + HISTORY_RECORD_FILE_NAME + currentDate + ".csv");
        try {
            csvFile.createNewFile();
            PrintWriter printWriter = new PrintWriter(csvFile);
            printWriter.print(csvResult.toString());
            printWriter.flush();
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
}
