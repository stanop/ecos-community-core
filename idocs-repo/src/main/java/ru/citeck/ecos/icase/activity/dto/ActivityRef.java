package ru.citeck.ecos.icase.activity.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import ru.citeck.ecos.records2.RecordRef;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActivityRef {

    private static String regexp = "(?<serviceType>\\w*\\$)?(?<processId>.*%)?(?<id>.*)";
    private static final Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);

    public static final ActivityRef EMPTY = new ActivityRef(null, RecordRef.EMPTY, "");
    public static final String ROOT_ID = "root";
    public static final String SERVICE_TYPE_DELIMITER = "$";
    public static final String PROCESS_ID_DELIMITER = "%";

    private CaseServiceType caseServiceType;
    private RecordRef processId;
    private String id;

    @JsonCreator
    public static ActivityRef of(String activityRef) {
        if (StringUtils.isBlank(activityRef)) {
            return EMPTY;
        }

        Matcher matcher = pattern.matcher(activityRef);
        if (!matcher.find()) {
            return EMPTY;
        }

        String rawCaseServiceType = matcher.group("serviceType");
        rawCaseServiceType = fixMatchingResult(rawCaseServiceType, '$');
        CaseServiceType caseServiceType = null;
        if (StringUtils.isNotBlank(rawCaseServiceType)) {
            caseServiceType = CaseServiceType.getByShortName(rawCaseServiceType);
        }

        String rawProcessId = matcher.group("processId");
        rawProcessId = fixMatchingResult(rawProcessId, '%');
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

    public static ActivityRef of(CaseServiceType caseServiceType, RecordRef processId, String id) {
        return new ActivityRef(caseServiceType, processId, id);
    }


    public CaseServiceType getCaseServiceType() {
        return caseServiceType;
    }

    public RecordRef getProcessId() {
        return processId;
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public boolean isRoot() {
        return ROOT_ID.equalsIgnoreCase(id);
    }

    @Override
    public String toString() {
        return (caseServiceType != null ? (caseServiceType.getShortName() + "$") : "")
            + (StringUtils.isNotBlank(processId.toString()) ? (processId.toString() + "%") : "")
            + id;
    }
}
