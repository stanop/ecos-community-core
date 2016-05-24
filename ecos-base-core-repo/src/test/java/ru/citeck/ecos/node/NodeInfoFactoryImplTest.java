package ru.citeck.ecos.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.alfresco.util.test.junitrules.TemporarySites.TestSiteAndMemberInfo;
import org.alfresco.util.test.junitrules.WellKnownNodes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class NodeInfoFactoryImplTest {

    private static Log logger = LogFactory.getLog(NodeInfoFactoryImplTest.class);
    private static NodeService nodeService;
    private static Repository repositoryHelper;
    private static ContentService contentService;
    private static NodeInfoFactory nodeInfoFactoryImpl;
    private static WorkflowService workflowService;
    private static PersonService personService;
    private static NamespaceService namespaceService;
    private TestSiteAndMemberInfo testSiteAndMemberInfo;

    private NodeRef testNode;
    private static NodeRef notExistNodeRef;
    private NodeRef folder;
    private String folderName;

    @ClassRule
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    @Rule
    public TemporarySites temporarySites = new TemporarySites(APP_CONTEXT_INIT);
    @Rule
    public WellKnownNodes wellKnownNodes = new WellKnownNodes(APP_CONTEXT_INIT);
    @Rule
    public TemporaryNodes tempNodes = new TemporaryNodes(APP_CONTEXT_INIT);

    private static RetryingTransactionHelper transactionHelper;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ApplicationContext context = APP_CONTEXT_INIT.getApplicationContext();
        ServiceRegistry serviceRegistry = context.getBean("ServiceRegistry", ServiceRegistry.class);
        nodeService = serviceRegistry.getNodeService();
        repositoryHelper = context.getBean("repositoryHelper", Repository.class);
        contentService = context.getBean("contentService", ContentService.class);
        nodeInfoFactoryImpl = context.getBean("NodeInfoFactory", NodeInfoFactory.class);
        transactionHelper = context.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        workflowService = context.getBean("WorkflowService", WorkflowService.class);
        personService = context.getBean("personService", PersonService.class);
        namespaceService = context.getBean("NamespaceService", NamespaceService.class);

        notExistNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                NodeInfoFactoryImplTest.class.getSimpleName() + "-not-exist-node-ref");
    }

    @Before
    public void setUp() throws Exception {
        final String siteShortName = this.getClass().getSimpleName() + "TestSite" + System.currentTimeMillis();

        testSiteAndMemberInfo = temporarySites.createTestSiteWithUserPerRole(siteShortName, "sitePreset",
                SiteVisibility.PUBLIC, AuthenticationUtil.getAdminUserName());

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        testNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                final NodeRef docLibNodeRef = testSiteAndMemberInfo.doclib;

                return nodeService.createNode(docLibNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
                        ContentModel.TYPE_CONTENT).getChildRef();
            }
        });
        NodeRef rootNode = nodeService.getRootNode(
                    new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        String guid = GUID.generate();
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderName = "testFolder" + guid;
        folderProps.put(ContentModel.PROP_NAME, this.folderName);
        this.folder = nodeService.createNode(
                rootNode,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder" + guid),
                ContentModel.TYPE_FOLDER,
                folderProps).getChildRef();
    }


    @Test
    public void testCreateNodeInfo() {
        NodeInfo nodeInfo = nodeInfoFactoryImpl.createNodeInfo();
        assertNotNull(nodeInfo);
        assertEquals("Properties are not empty", 0, nodeInfo.getProperties().size());
        assertEquals("TargetAssocs are not empty", 0, nodeInfo.getTargetAssocs().size());
        assertNull("SourceAssocs are not null", nodeInfo.getSourceAssocs());
        assertEquals("ChildAssocs are not empty", 0, nodeInfo.getChildAssocs().size());
    }

    @Test
    public void testCreateNodeInfoByNullNodeRef() {
        NodeInfo nodeInfo = nodeInfoFactoryImpl.createNodeInfo(notExistNodeRef);
        assertNull(nodeInfo);
    }

    @Test
    public void testCreateNodeInfoByNodeRef() {
        NodeRef contentRef = addTempScript(NodeInfoFactoryImplTest.class.getSimpleName() + ".js",
                "document.properties.name = \"Changed\" + \"_\" + document.properties.name;\ndocument.save();");

        NodeInfo nodeInfo = nodeInfoFactoryImpl.createNodeInfo(contentRef);
        assertNotNull(nodeInfo);
        assertEquals("TargetAssocs are not equals",
                nodeService.getTargetAssocs(contentRef, RegexQNamePattern.MATCH_ALL).size(),
                nodeInfo.getTargetAssocs().size());
        assertNull("Size of the SourceAssocs are not null", nodeInfo.getSourceAssocs());
        assertEquals("Size of the ChildAssocs are not equals",
                nodeService.getChildAssocs(contentRef).size(),
                nodeInfo.getChildAssocs().size());

        assertEquals("NodeRef is not equals", contentRef, nodeInfo.getNodeRef());
        assertEquals("Type is not equals", nodeService.getType(contentRef), nodeInfo.getType());
        assertEquals("Aspects are not equals",
                new ArrayList<>(nodeService.getAspects(contentRef)),
                nodeInfo.getAspects());
        assertEquals("Parent is not equals", nodeService.getPrimaryParent(contentRef).getParentRef(), nodeInfo.getParent());
        assertEquals("Properties are not equals", nodeService.getProperties(contentRef), nodeInfo.getProperties());
    }

    private NodeRef addTempScript(final String scriptFileName, final String javaScript) {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        logger.debug("start addTempScript");
        return transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
            public NodeRef execute() throws Throwable {
                NodeRef companyHomeRef = wellKnownNodes.getCompanyHome();
                logger.debug("companyHomeRef: " + companyHomeRef);

                // Create the script node reference
                NodeRef script = nodeService.createNode(companyHomeRef, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, scriptFileName),
                        ContentModel.TYPE_CONTENT).getChildRef();

                nodeService.setProperty(script, ContentModel.PROP_NAME, scriptFileName);

                ContentWriter contentWriter = contentService.getWriter(script, ContentModel.PROP_CONTENT, true);
                contentWriter.setMimetype(MimetypeMap.MIMETYPE_JAVASCRIPT);
                contentWriter.setEncoding("UTF-8");
                contentWriter.putContent(javaScript);

                tempNodes.addNodeRef(script);
                return script;
            }
        });
    }

    @Test
    public void testCreateNodeInfoByWorkflowTask() {
        WorkflowTask workflowTask = getWorkflowTask("activiti$activitiReview");

        NodeInfo nodeInfo = nodeInfoFactoryImpl.createNodeInfo(workflowTask);
        assertNotNull(nodeInfo);

        assertEquals("Type is not equals", QName.createQName(workflowTask.getName(), namespaceService), nodeInfo.getType());
    }

    private WorkflowTask getWorkflowTask(String definitionName)
    {
        WorkflowDefinition reviewDef = workflowService.getDefinitionByName(definitionName);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        NodeRef reviewer = personService.getPerson(AuthenticationUtil.getAdminUserName());
        properties.put(WorkflowModel.ASSOC_ASSIGNEE, reviewer);
        properties.put(WorkflowModel.ASSOC_PACKAGE, folder);
        WorkflowPath path = workflowService.startWorkflow(reviewDef.getId(), properties);
        WorkflowTask task = getTaskForPath(path);
        String startTaskId = reviewDef.getStartTaskDefinition().getId();
        if (startTaskId.equals(task.getDefinition().getId()))
        {
            workflowService.endTask(task.getId(), null);
            task = getTaskForPath(path);
        }
        return task;
    }

    private WorkflowTask getTaskForPath(WorkflowPath path)
    {
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks);
        assertTrue(tasks.size() > 0);
        WorkflowTask task = tasks.get(0);
        return task;
    }
}