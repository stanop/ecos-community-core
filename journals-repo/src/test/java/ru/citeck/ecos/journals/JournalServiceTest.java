package ru.citeck.ecos.journals;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class JournalServiceTest {
    private static JournalService journalService;
    private static NodeService nodeService;

    private static final ApplicationContext testContext = ApplicationContextHelper.getApplicationContext(
                new String[] { "classpath:alfresco/application-context.xml",
                               "classpath:alfresco/web-scripts-application-context.xml" });

    static final String CREATE_JOURNAL_FILE_NAME = "alfresco/create-journal.xml";

    @BeforeClass
    public static void setUpClass() throws Exception {
        ServiceRegistry serviceRegistry = testContext.getBean("ServiceRegistry", ServiceRegistry.class);
        nodeService = serviceRegistry.getNodeService();
        journalService = testContext.getBean("journalService", JournalService.class);
    }

    @Test
    public void testWiring() {
        assertNotNull(journalService);
    }

    @Test
    public void testWiringNodeService() {
        assertNotNull(nodeService);
    }

    @Test
    public void testGetAllJournalTypes() {
        Collection<JournalType> allJournalTypes = journalService.getAllJournalTypes();
        assertNotNull(allJournalTypes);
        assertFalse(allJournalTypes.size() < 2);

        List<String> guaranteedExistJournals = new ArrayList<>(2);
        guaranteedExistJournals.add("cm-content");
        guaranteedExistJournals.add("dl-dataListItem-test");

        short index =  0;
        for (JournalType type : allJournalTypes) {
            if (guaranteedExistJournals.contains(type.getId())) {
                index++;
            }
        }
        assertEquals(2, index);
    }

    @Test
    public void testPreInstalledJournalType() {
        String journalId = "cm-content";
        JournalType journalType = journalService.getJournalType(journalId);
        assertNotNull(journalType);

        assertEquals(journalId, journalType.getId());
        assertEquals(0, journalType.getAttributeOptions(ContentModel.PROP_NAME).size());

        assertEquals("cm:content", journalType.getOptions().get("type"));
    }

    @Test
    public void testCheckTestJournalFromBootstrap() {
        String journalId = "dl-dataListItem-test";
        JournalType journalType = journalService.getJournalType(journalId);
        assertNotNull(journalType);

        assertEquals(journalId, journalType.getId());
        assertEquals(6, journalType.getAttributes().size());
        assertEquals(0, journalType.getAttributeOptions(ContentModel.PROP_NAME).size());
        assertEquals(0, journalType.getAttributeOptions(ContentModel.PROP_MODIFIED).size());

        assertEquals(6, journalType.getDefaultAttributes().size());
        assertEquals(6, journalType.getVisibleAttributes().size());
        assertEquals(6, journalType.getSearchableAttributes().size());
        assertEquals(6, journalType.getSortableAttributes().size());
        assertEquals(0, journalType.getGroupableAttributes().size());

        assertEquals(3, journalType.getOptions().size());
        assertEquals("nodeRef", journalType.getOptions().get("doubleClickId"));
        assertEquals("card-details?nodeRef={id}", journalType.getOptions().get("doubleClickLink"));
        assertEquals("dl:dataListItem", journalType.getOptions().get("type"));
    }

    @Test
    public void testCreateJournal() throws IOException {
        Resource resource = new ClassPathResource(CREATE_JOURNAL_FILE_NAME);
        journalService.deployJournalTypes(resource.getInputStream());

        String journalId = "dl-dataListItem-create-test";
        JournalType journalType = journalService.getJournalType(journalId);

        assertNotNull(journalType);
        assertEquals(journalId, journalType.getId());
        assertEquals(6, journalType.getAttributes().size());
        assertEquals(0, journalType.getAttributeOptions(ContentModel.PROP_NAME).size());
        assertEquals(1, journalType.getAttributeOptions(ContentModel.PROP_MODIFIED).size());

        assertEquals(6, journalType.getDefaultAttributes().size());
        assertEquals(6, journalType.getVisibleAttributes().size());
        assertEquals(6, journalType.getSearchableAttributes().size());
        assertEquals(6, journalType.getSortableAttributes().size());
        assertEquals(0, journalType.getGroupableAttributes().size());

        assertEquals(3, journalType.getOptions().size());
        assertEquals("nodeRef", journalType.getOptions().get("doubleClickId"));
        assertEquals("card-details?nodeRef={id}", journalType.getOptions().get("doubleClickLink"));
        assertEquals("dl:dataListItem", journalType.getOptions().get("type"));
    }
}
