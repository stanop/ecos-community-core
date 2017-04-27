package ru.citeck.ecos.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleTransition;
import ru.citeck.ecos.model.IdocsModel;
//import ru.citeck.ecos.test.ApplicationContextHelper;

/**
 * @author: Alexander Nemerov
 * @date: 26.02.14
 */
public class LifeCycleServiceImplTest {

    private static final String LIFECYCLE_AUTO_TRANSITION_FILE_NAME = "alfresco/lifecycle/lc-auto-transition-test.xml";
    private static final String LIFECYCLE_TIMER_TRANSITION_FILE_NAME = "alfresco/lifecycle/lc-on-timer-transition-test.xml";
    private static final String LIFECYCLE_TIMER_TRANSITION_NEGATIVE_FILE_NAME = "alfresco/lifecycle/lc-on-timer-transition-negative-test.xml";
    private static final String LIFECYCLE_USER_TRANSITION_FILE_NAME = "alfresco/lifecycle/lc-user-transition-test.xml";
    private static final String LIFECYCLE_PROCESS_TRANSITION_FILE_NAME = "alfresco/lifecycle/lc-start-end-process-transition-test.xml";

    private static LifeCycleServiceImpl lifeCycleService;
    private static NodeService nodeService;
    private static ContentService contentService;
    private static Repository repositoryHelper;
    private static NodeRef testTableRef = null;
    private static WorkflowService workflowService;
    private static NamespaceService namespaceService;
    private static PersonService personService;

    private NodeRef docRef;

    @BeforeClass
    public static void setUpClass() throws Exception {
//        ApplicationContext context = ApplicationContextHelper.getApplicationContext();
//        lifeCycleService = context.getBean("lifeCycleService", LifeCycleServiceImpl.class);
//        ServiceRegistry serviceRegistry = context.getBean("ServiceRegistry", ServiceRegistry.class);
//        repositoryHelper = context.getBean("repositoryHelper", Repository.class);
//        nodeService = serviceRegistry.getNodeService();
//        contentService = serviceRegistry.getContentService();
//        workflowService = serviceRegistry.getWorkflowService();
//        namespaceService = serviceRegistry.getNamespaceService();
//        personService = serviceRegistry.getPersonService();
//
//        // create test root folder
//        String userName = AuthenticationUtil.getRunAsUser();
//        if (userName == null) {
//            userName = "admin";
//        }
//        runAsSystem();
////        createTestTableCSV();
////        createTestTableXML();
//        AuthenticationUtil.setRunAsUser(userName);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
//        if (testTableRef == null) {
//            return;
//        }
//        nodeService.deleteNode(testTableRef);
    }

    @Before
    public void setUp() throws Exception {
//        NodeRef companyHome = repositoryHelper.getCompanyHome();
//        ChildAssociationRef childRef = nodeService.createNode(companyHome,
//                ContentModel.ASSOC_CONTAINS,
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
//                        LifeCycleServiceImplTest.class.getSimpleName()),
//                IdocsModel.TYPE_DOC);
//        docRef = childRef.getChildRef();
    }

    @After
    public void tearDown() throws Exception {
//        if (docRef == null) {
//            return;
//        }
//        nodeService.deleteNode(docRef);
//        undeployTestLifeCycle();
    }

    private static void runAsSystem() {
        AuthenticationUtil.setRunAsUserSystem();
    }

    private static void createTestTableCSV() throws IOException {
        String filePath = "alfresco/lifecycle/lifecycletest.csv";
        ClassPathResource resource = new ClassPathResource(filePath);
        assertNotNull("CSV file not found", resource);

        lifeCycleService.deployLifeCycle(resource.getInputStream(), LifeCycleCSVFormat.NAME, IdocsModel.TYPE_DOC, filePath);
    }

    private static void createTestTableXML() throws IOException {
        String filePath = "alfresco/lifecycle/lifecycletest.xml";
        ClassPathResource resource = new ClassPathResource(filePath);
        assertNotNull("XML file not found", resource);

        lifeCycleService.deployLifeCycle(resource.getInputStream(), LifeCycleXMLFormat.NAME, IdocsModel.TYPE_DOC, filePath);
    }

    private static void deployTestLifeCycle(String lifeCycleFilePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(lifeCycleFilePath);
        assertNotNull("LifeCycle file \"" + lifeCycleFilePath + "\" not found", resource);

        lifeCycleService.deployLifeCycle(resource.getInputStream(), LifeCycleXMLFormat.NAME, IdocsModel.TYPE_DOC, lifeCycleFilePath);
    }

    private static void undeployTestLifeCycle() {
        lifeCycleService.undeployLifeCycle(IdocsModel.TYPE_DOC);
    }

    private static NodeRef createTestDoc() throws Exception {
        NodeRef companyHome = repositoryHelper.getCompanyHome();
        ChildAssociationRef childRef = nodeService.createNode(companyHome,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                        LifeCycleServiceImplTest.class.getSimpleName()),
                IdocsModel.TYPE_DOC);
        return childRef.getChildRef();
    }

    private static void deleteTestDoc(NodeRef testDoc) throws Exception {
        if (testDoc != null) {
            nodeService.deleteNode(testDoc);
        }
    }

    public static void setContentService(ContentService contentService) {
        LifeCycleServiceImplTest.contentService = contentService;
    }


    @Test
    @Ignore
    public void testDoTransition() throws Exception {
//        List<LifeCycleTransition> events = lifeCycleService.getAvailableUserEvents(docRef);
//
//        if (events.size() == 1) {
//            lifeCycleService.doTransition(docRef, events.get(0), null, null); //TODO
//            String newState = lifeCycleService.getDocumentState(docRef);
//            assertEquals("confirmx1", newState);
//        }
    }

    /**
     * Test auto transitions at lifecycle
     *
     * @throws Exception
     */
    @Test
    public void testAutoTransition() throws Exception {
//        deployTestLifeCycle(LIFECYCLE_AUTO_TRANSITION_FILE_NAME);
//        String state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("start", state);
//
//        List<LifeCycleTransition> transitions = lifeCycleService.getTransitionsByDocState(docRef);
//        assertEquals(1, transitions.size());
//
//        lifeCycleService.doTransition(docRef, transitions.get(0), null, null);
//        state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("endState", state);
    }

    /**
     * Test positive time transition at lifecycle
     *
     * @throws Exception
     */
    @Test
    public void testTimerTransition() throws Exception {
//        deployTestLifeCycle(LIFECYCLE_TIMER_TRANSITION_FILE_NAME);
//        String state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("start", state);
//
//        List<LifeCycleTransition> transitions = lifeCycleService.getTransitionsByDocState(docRef);
//        assertEquals(1, transitions.size());
//
//        lifeCycleService.doTransition(docRef, transitions.get(0), null, null);
//        state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("endState", state);
    }

    /**
     * Test negative time transition at lifecycle
     *
     * @throws Exception
     */
    @Test
    public void testTimerTransitionNegative() throws Exception {
//        deployTestLifeCycle(LIFECYCLE_TIMER_TRANSITION_NEGATIVE_FILE_NAME);
//        String state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("start", state);
//
//        List<LifeCycleTransition> transitions = lifeCycleService.getTransitionsByDocState(docRef);
//        assertEquals(1, transitions.size());
//
//        lifeCycleService.doTransition(docRef, transitions.get(0), null, null);
//        state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("waitTimerState", state);
    }

    /**
     * Test user transition at lifecycle
     *
     * @throws Exception
     */
    @Test
    public void testUserTransition() throws Exception {
//        deployTestLifeCycle(LIFECYCLE_USER_TRANSITION_FILE_NAME);
//
//        String state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("start", state);
//
//        List<LifeCycleTransition> transitions = lifeCycleService.getTransitionsByDocState(docRef);
//        assertEquals(1, transitions.size());
//
//        lifeCycleService.doTransition(docRef, transitions.get(0), null, null);
//        transitions = lifeCycleService.getAvailableUserEvents(docRef);
//        assertEquals(4, transitions.size());
//
//        lifeCycleService.doTransition(docRef, transitions.get(1), null, null);
//        state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("endState", state);
    }

    /**
     * Test start and end proccess transition at lifecycle
     *
     * @throws Exception
     */
    @Test
    public void testProcessTransition() throws Exception {
//        deployTestLifeCycle(LIFECYCLE_PROCESS_TRANSITION_FILE_NAME);
//
//        NodeRef wfPackage = workflowService.createPackage(null);
//        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>();
//        workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
//        workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "Тестовый процесс");
//        QName key = QName.createQName("wfgfam:people", namespaceService);
//        Serializable value = new ArrayList<>(Collections.singleton(personService.getPerson("admin")));
//        workflowProps.put(key, value);
//        WorkflowDefinition wfDefinition = workflowService.getDefinitionByName("activiti$familiarization");
//
//        String state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("start", state);
//
//        List<LifeCycleTransition> transitions = lifeCycleService.getTransitionsByDocState(docRef);
//        assertEquals(1, transitions.size());
//
//        lifeCycleService.doTransition(docRef, transitions.get(0), null, null);
//        state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("waitProcessStart", state);
//
//        WorkflowPath wfPath = workflowService.startWorkflow(wfDefinition.getId(), workflowProps);
//        transitions = lifeCycleService.getTransitionsByDocState(docRef);
//        assertEquals(1, transitions.size());
//
//        lifeCycleService.doTransition(docRef, transitions.get(0), null, null);
//        state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("waitProcessEnd", state);
//
//        workflowService.cancelWorkflow(wfPath.getId());
//        transitions = lifeCycleService.getTransitionsByDocState(docRef);
//        assertEquals(1, transitions.size());
//
//        lifeCycleService.doTransition(docRef, transitions.get(0), null, null);
//        state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("endState", state);
    }

    @Test
    @Ignore
    public void testGetCheckStartState() throws Exception {
//        String state = lifeCycleService.getDocumentState(docRef);
//        assertEquals("start", state);
//        List<LifeCycleTransition> transitions = lifeCycleService.getTransitionsByDocState(docRef);
//        state = lifeCycleService.getDocumentState(docRef);
    }

}
