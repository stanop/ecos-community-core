package ru.citeck.ecos.calendar.eform;

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;

/**
 * @author Roman Makarskiy
 */
public interface EcosCalendarEntry extends CalendarEntry {
    void setType(NodeRef type);

    NodeRef getType();

    void setParticipants(ArrayList<String> participants);

    ArrayList<String> getParticipants();

    void setHasSyncConflicts(boolean hasSyncConflicts);

    boolean getHasSyncConflicts();

    void setLinkToEntry(String linkToEntry);

    String getLinkToEntry();
}
