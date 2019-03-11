package ru.citeck.ecos.calendar.service;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.calendar.eform.EcosCalendarEntry;
import ru.citeck.ecos.calendar.eform.EcosCalendarEntryDTO;

/**
 * @author Roman Makarskiy
 */
public interface EcosCalendarService {

    /**
     * Get NodeRef of container by calendar id
     *
     * @param calendarId calendar id
     * @return container NodeRef
     */
    NodeRef getCalendarContainer(String calendarId);

    /**
     * Stores a new {@link EcosCalendarEntry} into the given site or user.
     * The concrete class {@link EcosCalendarEntryDTO} can be used
     * to create a {@link EcosCalendarEntry} instance for this.
     *
     * @return The newly created EcosCalendarEntry
     */
    @NotAuditable
    EcosCalendarEntry createCalendarEntry(String containerId, EcosCalendarEntry entry);

    /**
     * Updates an existing {@link EcosCalendarEntry} in the repository.
     *
     * @return The updated EcosCalendarEntry
     */
    @NotAuditable
    EcosCalendarEntry updateCalendarEntry(EcosCalendarEntry entry, String containerId);

    /**
     * Deletes an existing {@link EcosCalendarEntry} from the repository
     */
    @NotAuditable
    void deleteCalendarEntry(EcosCalendarEntry entry);

    /**
     * Retrieves an existing {@link EcosCalendarEntry} from the repository
     */
    @NotAuditable
    EcosCalendarEntry getCalendarEntry(String containerId, String entryName);
}
