package ru.citeck.ecos.history.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private static final String SEND_NEW_RECORD_QUEUE = "send_new_record_queue";

    /**
     * Path constants
     */
    private static final String GET_BY_DOCUMENT_ID_PATH = "/history_records/by_document_id/";
    private static final String INSERT_RECORD_PATH = "/history_records/insert_record";

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
    @Value("${ecos.citek.history.service.use.activemq}")
    private Boolean useActiveMq;

    /**
     * Services
     */
    private RestTemplate restTemplate;

    private RabbitTemplate rabbitTemplate;

    /**
     * Get history records
     * @param documentUuid Document uuid
     * @return List of maps
     */
    @Override
    public List getHistoryRecords(String documentUuid) {
        return restTemplate.getForObject(historyServiceHost + GET_BY_DOCUMENT_ID_PATH + documentUuid, List.class);
    }

    /**
     * Send history event to remote service
     * @param requestParams Request params
     */
    @Override
    public void sendHistoryEventToRemoteService(Map<String, Object> requestParams) {
        try {
            if (useActiveMq) {
                convertMapToJsonString(requestParams);
                rabbitTemplate.convertAndSend(SEND_NEW_RECORD_QUEUE, convertMapToJsonString(requestParams));
            } else {
                restTemplate.postForObject(historyServiceHost + INSERT_RECORD_PATH, requestParams, String.class);
            }

        } catch (Exception exception) {
            logger.error(exception);
            saveHistoryRecordAsCsv(requestParams);
        }
    }

    /**
     * Convert map to json string
     * @param requestParams Request params map
     * @return Json string
     */
    private String convertMapToJsonString(Map<String, Object> requestParams) {
        JSONObject jsonObject =  new JSONObject(requestParams);
        return jsonObject.toString();
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

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
}
