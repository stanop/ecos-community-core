package ru.citeck.ecos.icase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.*;
import org.junit.runner.RunWith;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import ru.citeck.ecos.icase.element.CaseElementDAO;
//import ru.citeck.ecos.test.ApplicationContextHelper;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(loader=ApplicationContextHelper.class)
@Transactional
@Ignore
public abstract class BaseCaseStrategyTest {

    protected ApplicationContext applicationContext;
    protected CaseElementDAO strategy;
    protected ServiceRegistry serviceRegistry;
    protected NodeService nodeService;
    protected PersonService personService;

    protected NodeRef rootNode;
    protected NodeRef caseNode;
    protected NodeRef testNode1, testNode2;
    protected NodeRef configNode;


    @Before
    public void setUp() {
//        applicationContext = ApplicationContextHelper.getApplicationContext();
//        serviceRegistry = applicationContext.getBean("ServiceRegistry", ServiceRegistry.class);
//        nodeService = serviceRegistry.getNodeService();
//        personService = serviceRegistry.getPersonService();
//        AuthenticationUtil.setRunAsUserSystem();
//        rootNode = nodeService.createNode(nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE),
//                ContentModel.ASSOC_CHILDREN, QName.createQName(ICaseModel.NAMESPACE, getClass().getSimpleName()),
//                ContentModel.TYPE_CONTAINER).getChildRef();
//
//        caseNode = nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN,
//                QName.createQName(ICaseModel.NAMESPACE, this.getClass().getName() + "-case"),
//                ContentModel.TYPE_FOLDER).getChildRef();
//        testNode1 = nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN, QName.createQName(ICaseModel.NAMESPACE, "test1"),
//                ContentModel.TYPE_CONTENT).getChildRef();
//        testNode2 = nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN, QName.createQName(ICaseModel.NAMESPACE, "test2"),
//                ContentModel.TYPE_CONTENT).getChildRef();
//        configNode = createConfigNode();
//
//        strategy = createStrategy();
    }

    @After
    public void tearDown() {
//        dropNodes(rootNode);
//        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchWithNullCase() {
//        strategy.get(null, configNode);
//        fail("Searches with null case NodeRef");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchWithNullConfig() {
//        strategy.get(caseNode, null);
//        fail("Searches with null elementConfig NodeRef");
    }

    @Test
    public void testStrategySanity() {
        
//        List<NodeRef> elements;
//
//        elements = strategy.get(caseNode, configNode);
//        assertTrue(elements.isEmpty());
//
//        strategy.add(testNode1, caseNode, configNode);
//        assertEquals(createSet(testNode1), toSet(strategy.get(caseNode, configNode)));
//
//        strategy.add(testNode2, caseNode, configNode);
//        assertEquals(createSet(testNode1, testNode2), toSet(strategy.get(caseNode, configNode)));
//
//        strategy.remove(testNode1, caseNode, configNode);
//        assertEquals(createSet(testNode2), toSet(strategy.get(caseNode, configNode)));
//
//        strategy.remove(testNode2, caseNode, configNode);
//        assertTrue(elements.isEmpty());
        
    }
    
    private Object toSet(Collection<NodeRef> list) {
        return new HashSet<>(list);
    }

    @SafeVarargs
    protected static <E> Set<E> createSet(E... elements) {
        Set<E> result = new HashSet<E>(elements.length);
        for(E element : elements) {
            result.add(element);
        }
        return result;
    }
    
    protected void dropNode(NodeRef node) {
        if (node != null && nodeService.exists(node))
            nodeService.deleteNode(node);
    }

    protected void dropNodes(NodeRef... nodes) {
        for (NodeRef node : nodes) {
            dropNode(node);
        }
    }
    
    protected abstract NodeRef createConfigNode();

    protected abstract CaseElementDAO createStrategy();

}
