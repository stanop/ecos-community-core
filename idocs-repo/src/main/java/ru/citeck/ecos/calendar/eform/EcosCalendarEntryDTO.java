package ru.citeck.ecos.calendar.eform;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.records.RecordConstants;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;

import java.io.Serializable;
import java.util.*;

/**
 * @author Roman Makarskiy
 */
public class EcosCalendarEntryDTO implements EcosCalendarEntry, MetaValue, Serializable {

    private static final long serialVersionUID = 842866928058323701L;

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final String UTC_MIDNIGHT = "T00:00:00Z";

    private NodeRef nodeRef;
    private NodeRef containerNodeRef;
    private String systemName;

    private String title;
    private String description;
    private String location;
    private Date start;
    private Date end;
    private String recurrenceRule;
    private Date lastRecurrence;
    private String sharePointDocFolder;
    private boolean isOutlook = false;
    private String outlookUID;
    private Date createdAt;
    private Date modifiedAt;
    private List<String> tags = new ArrayList<>();

    private NodeRef type;
    private ArrayList<String> participants = new ArrayList<>();
    private boolean hasSyncConflicts;
    private String linkToEntry;

    public EcosCalendarEntryDTO() {
    }

    public static boolean isAllDay(EcosCalendarEntry entry) {
        return isAllDay(entry.getStart(), entry.getEnd());
    }

    public static boolean isAllDay(Date start, Date end) {
        if (start == null || end == null) {
            // One or both dates is missing
            return false;
        }

        // As of 4.0, all-day events use UTC midnight for consistency
        Calendar startUTC = Calendar.getInstance();
        Calendar endUTC = Calendar.getInstance();
        startUTC.setTime(start);
        endUTC.setTime(end);
        startUTC.setTimeZone(UTC);
        endUTC.setTimeZone(UTC);

        // Pre-4.0, the midnights were local time...
        Calendar startLocal = Calendar.getInstance();
        Calendar endLocal = Calendar.getInstance();
        startLocal.setTime(start);
        endLocal.setTime(end);

        // Check for midnight, first in UTC then again in Server Local Time
        Calendar[] starts = new Calendar[]{startUTC, startLocal};
        Calendar[] ends = new Calendar[]{endUTC, endLocal};
        for (int i = 0; i < starts.length; i++) {
            Calendar startLoc = starts[i];
            Calendar endLoc = ends[i];
            if (startLoc.get(Calendar.HOUR_OF_DAY) == 0 &&
                    startLoc.get(Calendar.MINUTE) == 0 &&
                    startLoc.get(Calendar.SECOND) == 0 &&
                    endLoc.get(Calendar.HOUR_OF_DAY) == 0 &&
                    endLoc.get(Calendar.MINUTE) == 0 &&
                    endLoc.get(Calendar.SECOND) == 0) {
                // Both at midnight, counts as all day
                return true;
            }
        }

        // In any other case, it isn't an all-day
        return false;
    }

    public static Date extractDate(boolean isAllDay, String strDate) {
        if (StringUtils.isBlank(strDate)) {
            throw new IllegalArgumentException("Missing date. Check you params");

        }

        if (isAllDay) {
            strDate = ensureStoreToUtcMidnight(strDate);
        }

        return CalendarDateParser.parseDate(strDate);
    }

    private static String ensureStoreToUtcMidnight(String date) {
        return date.substring(0, 10) + UTC_MIDNIGHT;
    }

    @Override
    public Object getAttribute(String attributeName) {
        switch (attributeName) {
            case CalendarEntryAttrs.ATT_TITLE:
                return this.getTitle();
            case CalendarEntryAttrs.ATT_DESCRIPTION:
                return this.getDescription();
            case CalendarEntryAttrs.ATT_LOCATION:
                return this.getLocation();
            case CalendarEntryAttrs.ATT_ALL_DAY:
                return isAllDay(this.getStart(), this.getEnd());
            case CalendarEntryAttrs.ATT_START:
                if (this.getStart() == null) {
                    return null;
                }
                return ISO8601DateFormat.format(this.getStart());
            case CalendarEntryAttrs.ATT_END:
                if (this.getEnd() == null) {
                    return null;
                }
                return ISO8601DateFormat.format(this.getEnd());
            case CalendarEntryAttrs.ATT_CREATED_AT:
                if (this.getCreatedAt() == null) {
                    return null;
                }
                return ISO8601DateFormat.format(this.getCreatedAt());
            case CalendarEntryAttrs.ATT_MODIFIED_AT:
                if (this.getModifiedAt() == null) {
                    return null;
                }
                return ISO8601DateFormat.format(this.getModifiedAt());
            case CalendarEntryAttrs.ATT_TYPE:
                return getType();
            case CalendarEntryAttrs.ATT_PARTICIPANTS:
                ArrayNode jsonNodes = JsonNodeFactory.instance.arrayNode();
                List<String> participants = getParticipants();
                participants.forEach(jsonNodes::add);
                return jsonNodes;
            case CalendarEntryAttrs.ATT_HAS_SYNC_CONFLICTS:
                return getHasSyncConflicts();
            case CalendarEntryAttrs.ATT_LINK_TO_ENTRY:
                return getLinkToEntry();
            case RecordConstants.ATT_FORM_KEY:
                return "FORM_KEY_FOR_EVENT";
        }

        return null;
    }

    @Override
    public String getId() {
        return this.nodeRef != null ? nodeRef.toString() : "";
    }


    @Override
    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    @Override
    public NodeRef getContainerNodeRef() {
        return containerNodeRef;
    }

    public void setContainerNodeRef(NodeRef containerNodeRef) {
        this.containerNodeRef = containerNodeRef;
    }

    @Override
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public Date getStart() {
        return start;
    }

    @Override
    public void setStart(Date start) {
        this.start = start;
    }

    @Override
    public Date getEnd() {
        return end;
    }

    @Override
    public void setEnd(Date end) {
        this.end = end;
    }

    @Override
    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    @Override
    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    @Override
    public Date getLastRecurrence() {
        return lastRecurrence;
    }

    @Override
    public void setLastRecurrence(Date lastRecurrence) {
        this.lastRecurrence = lastRecurrence;
    }

    @Override
    public String getSharePointDocFolder() {
        return sharePointDocFolder;
    }

    @Override
    public void setSharePointDocFolder(String sharePointDocFolder) {
        this.sharePointDocFolder = sharePointDocFolder;
    }

    @Override
    public boolean isOutlook() {
        return isOutlook;
    }

    @Override
    public void setOutlook(boolean outlook) {
        isOutlook = outlook;
    }

    @Override
    public String getOutlookUID() {
        return outlookUID;
    }

    @Override
    public void setOutlookUID(String outlookUID) {
        this.outlookUID = outlookUID;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public NodeRef getType() {
        return type;
    }

    @Override
    public void setType(NodeRef type) {
        this.type = type;
    }

    @Override
    public ArrayList<String> getParticipants() {
        return participants;
    }

    @Override
    public void setHasSyncConflicts(boolean hasSyncConflicts) {
        this.hasSyncConflicts = hasSyncConflicts;
    }

    @Override
    public boolean getHasSyncConflicts() {
        return hasSyncConflicts;
    }

    @Override
    public void setLinkToEntry(String linkToEntry) {
        this.linkToEntry = linkToEntry;
    }

    @Override
    public String getLinkToEntry() {
        return linkToEntry;
    }

    @Override
    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return "EcosCalendarEntryDTO{" +
                "nodeRef=" + nodeRef +
                ", containerNodeRef=" + containerNodeRef +
                ", systemName='" + systemName + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", recurrenceRule='" + recurrenceRule + '\'' +
                ", lastRecurrence=" + lastRecurrence +
                ", sharePointDocFolder='" + sharePointDocFolder + '\'' +
                ", isOutlook=" + isOutlook +
                ", outlookUID='" + outlookUID + '\'' +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", tags=" + tags +
                ", type=" + type +
                ", participants=" + participants +
                ", hasSyncConflicts=" + hasSyncConflicts +
                ", linkToEntry='" + linkToEntry + '\'' +
                '}';
    }
}
