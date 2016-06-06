package ru.citeck.ecos.journals;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertNotNull;

public class JournalServiceTest {
    private static JournalService journalService;
    private static NodeService nodeService;

    private static final ApplicationContext testContext = ApplicationContextHelper.getApplicationContext(
                new String[] { "classpath:alfresco/application-context.xml",
                               "classpath:alfresco/web-scripts-application-context.xml" });


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

}
