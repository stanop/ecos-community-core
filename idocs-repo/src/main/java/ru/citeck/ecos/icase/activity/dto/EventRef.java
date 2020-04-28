package ru.citeck.ecos.icase.activity.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import ru.citeck.ecos.records2.RecordRef;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventRef {

    private static String REGEXP = "(?<serviceType>\\w*\\$)?(?<processId>.*%)?(?<id>.*)";
    private static final Pattern PATTERN = Pattern.compile(REGEXP, Pattern.CASE_INSENSITIVE);

    public static final EventRef EMPTY = new EventRef(null, RecordRef.EMPTY, "");
    public static final String ROOT_ID = "root";
    public static final char SERVICE_TYPE_DELIMITER = '$';
    public static final char PROCESS_ID_DELIMITER = '%';

    @Getter private CaseServiceType caseServiceType;
    @Getter private RecordRef processId;
    @Getter private String id;

    @JsonCreator
    @ecos.com.fasterxml.jackson210.annotation.JsonCreator
    public static EventRef of(String eventRef) {
        if (StringUtils.isBlank(eventRef)) {
            return EMPTY;
        }

        Matcher matcher = PATTERN.matcher(eventRef);
        if (!matcher.find()) {
            return EMPTY;
        }

        String rawCaseServiceType = matcher.group("serviceType");
        rawCaseServiceType = fixMatchingResult(rawCaseServiceType, SERVICE_TYPE_DELIMITER);
        CaseServiceType caseServiceType = null;
        if (StringUtils.isNotBlank(rawCaseServiceType)) {
            caseServiceType = CaseServiceType.getByShortName(rawCaseServiceType);
        }

        String rawProcessId = matcher.group("processId");
        rawProcessId = fixMatchingResult(rawProcessId, PROCESS_ID_DELIMITER);
        RecordRef processId = RecordRef.valueOf(rawProcessId);

        String id = matcher.group("id");

        return of(caseServiceType, processId, id);
    }

    private static String fixMatchingResult(String str, char delimiter) {
        if (str == null) {
            return "";
        }

        return removeDelimiter(str, delimiter);
    }

    private static String removeDelimiter(String str, char delimiter) {
        if (str.charAt(str.length() - 1) == delimiter) {
            return str.substring(0, str.length() - 1);
        }

        return str;
    }

    public static EventRef of(CaseServiceType caseServiceType, RecordRef processId, String id) {
        return new EventRef(caseServiceType, processId, id);
    }

    @JsonIgnore
    @ecos.com.fasterxml.jackson210.annotation.JsonIgnore
    public boolean isRoot() {
        return ROOT_ID.equalsIgnoreCase(id);
    }

    @Override
    @JsonValue
    @ecos.com.fasterxml.jackson210.annotation.JsonValue
    public String toString() {
        return (caseServiceType != null ? (caseServiceType.getShortName() + SERVICE_TYPE_DELIMITER) : "")
            + (StringUtils.isNotBlank(processId.toString()) ? (processId.toString() + PROCESS_ID_DELIMITER) : "")
            + id;
    }
}
