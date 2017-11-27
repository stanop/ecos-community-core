package ru.citeck.ecos.journals;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;
import ru.citeck.ecos.journals.xml.Journals;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XmlObjectTest {
    private static JournalServiceImpl journalService;
    private static NodeService nodeService;

    private static final ApplicationContext testContext = ApplicationContextHelper.getApplicationContext(
                new String[] { "classpath:alfresco/application-context.xml",
                               "classpath:alfresco/web-scripts-application-context.xml" });

    @BeforeClass
    public static void setUpClass() throws Exception {
        ServiceRegistry serviceRegistry = testContext.getBean("ServiceRegistry", ServiceRegistry.class);
        nodeService = serviceRegistry.getNodeService();
        journalService = testContext.getBean("journalService", JournalServiceImpl.class);
    }

    @Test
    public void testWiring() {
        assertNotNull(journalService);
    }

    @Test
    public void testMarshalUnmarshalJournal() throws IOException, JAXBException, SAXException {

        Resource resource = new ClassPathResource(JournalServiceTest.CREATE_JOURNAL_FILE_NAME);
        Journals journals = journalService.parseXML(resource.getInputStream());

        JAXBContext jaxbContext = JAXBContext.newInstance(Journals.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = schemaFactory.newSchema(new ClassPathResource(JournalServiceImpl.JOURNALS_SCHEMA_LOCATION).getFile());
        marshaller.setSchema(schema);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(journals, out);
        byte[] bytes = out.toByteArray();
        Journals journalsClone = journalService.parseXML(new ByteArrayInputStream(bytes));

        assertEquals(journals.getImports().getImport().size(), journalsClone.getImports().getImport().size());
        assertEquals(journals.getJournal().size(), journalsClone.getJournal().size());

        assertEquals(journals, journalsClone);
    }

}
