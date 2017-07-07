package ru.citeck.ecos.constants;

import javafx.util.Pair;

/**
 * Document history constants
 */
public class DocumentHistoryConstants {

    public static final Pair<String, String> NODE_REF = new Pair<>("nodeRef", "historyEventId");
    public static final Pair<String, String> EVENT_INITIATOR = new Pair<>("event:initiator", "username");
    public static final Pair<String, String> DOCUMENT_ID = new Pair<>("event:documentId", "documentId");
    public static final Pair<String, String> DOCUMENT_DATE = new Pair<>("event:date", "creationTime");
    public static final Pair<String, String> DOCUMENT_VERSION = new Pair("event:documentVersion", "version");
    public static final Pair<String, String> COMMENTS = new Pair("event:taskComment", "comments");
    public static final Pair<String, String> EVENT_TYPE = new Pair<>("event:name", "eventType");

}
