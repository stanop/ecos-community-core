package ru.citeck.ecos.constants;

import java.util.AbstractMap;

/**
 * Document history constants
 */
public class DocumentHistoryConstants {

    public static final AbstractMap.SimpleEntry<String, String> NODE_REF = new AbstractMap.SimpleEntry<>("nodeRef", "historyEventId");
    public static final AbstractMap.SimpleEntry<String, String> EVENT_INITIATOR = new AbstractMap.SimpleEntry<>("event:initiator", "username");
    public static final AbstractMap.SimpleEntry<String, String> DOCUMENT_ID = new AbstractMap.SimpleEntry<>("event:documentId", "documentId");
    public static final AbstractMap.SimpleEntry<String, String> DOCUMENT_DATE = new AbstractMap.SimpleEntry<>("event:date", "creationTime");
    public static final AbstractMap.SimpleEntry<String, String> DOCUMENT_VERSION = new AbstractMap.SimpleEntry("event:documentVersion", "version");
    public static final AbstractMap.SimpleEntry<String, String> COMMENTS = new AbstractMap.SimpleEntry("event:taskComment", "comments");
    public static final AbstractMap.SimpleEntry<String, String> EVENT_TYPE = new AbstractMap.SimpleEntry<>("event:name", "eventType");
    public static final AbstractMap.SimpleEntry<String, String> TASK_ROLE = new AbstractMap.SimpleEntry<>("event:taskRole", "taskRole");
    public static final AbstractMap.SimpleEntry<String, String> TASK_OUTCOME = new AbstractMap.SimpleEntry<>("event:taskOutcome", "taskOutcome");
    public static final AbstractMap.SimpleEntry<String, String> TASK_TYPE = new AbstractMap.SimpleEntry<>("event:taskType", "taskType");

}
