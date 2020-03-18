package ru.citeck.ecos.icase.activity.dto;

import org.junit.Test;

import static org.junit.Assert.*;

public class ActivityRefTest {

    @Test
    public void of() {
        ActivityRef ref1 = ActivityRef.of("alf$alfresco/@workspace://SpacesStore/222-333%alfresco/@workspace://SpacesStore/111-444");
        assertEquals(ref1.getCaseServiceType(), CaseServiceType.ALFRESCO);
        assertEquals(ref1.getProcessId().toString(), "alfresco/@workspace://SpacesStore/222-333");
        assertEquals(ref1.getId(), "alfresco/@workspace://SpacesStore/111-444");

        ActivityRef ref2 = ActivityRef.of("eproc$alfresco/@workspace://SpacesStore/333-444%eproc/activity@654");
        assertEquals(ref2.getCaseServiceType(), CaseServiceType.EPROC);
        assertEquals(ref2.getProcessId().toString(), "alfresco/@workspace://SpacesStore/333-444");
        assertEquals(ref2.getId(), "eproc/activity@654");

        ActivityRef ref3 = ActivityRef.of("workspace://SpacesStore/333-444%workspace://SpacesStore/555-666");
        assertNull(ref3.getCaseServiceType());
        assertEquals(ref3.getProcessId().toString(), "workspace://SpacesStore/333-444");
        assertEquals(ref3.getId(), "workspace://SpacesStore/555-666");
    }

}
