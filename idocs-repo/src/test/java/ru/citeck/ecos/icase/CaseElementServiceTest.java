package ru.citeck.ecos.icase;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import ru.citeck.ecos.icase.element.CaseElementPolicies;
import ru.citeck.ecos.icase.element.CaseElementServiceImpl;
import ru.citeck.ecos.icase.element.config.ElementConfigDto;
import ru.citeck.ecos.model.ICaseModel;
//import ru.citeck.ecos.test.ApplicationContextHelper;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(loader=ApplicationContextHelper.class)
@Transactional
public class CaseElementServiceTest implements 
        CaseElementPolicies.OnCaseElementAddPolicy,
        CaseElementPolicies.OnCaseElementUpdatePolicy,
        CaseElementPolicies.OnCaseElementRemovePolicy
{

    private static final String TEST_CONFIG_NAME = CaseConstants.ASSOCIATION_ELEMENTS;
    private ApplicationContext applicationContext;
    private NodeService nodeService;
    private CaseElementServiceImpl caseElementService;
    private PolicyComponent policyComponent;
    
    private NodeRef rootNode;
    private NodeRef caseNode1, caseNode2;
    private NodeRef testNode1, testNode2;
    private ElementConfigDto testConfig;
    
    private Set<BehaviourCall> additions, updates, removals;
    private BehaviourDefinition<ClassBehaviourBinding> caseElementAddDefinition;
    private BehaviourDefinition<ClassBehaviourBinding> caseElementUpdateDefinition;
    private BehaviourDefinition<ClassBehaviourBinding> caseElementRemoveDefinition;
    
    /*@Before
    public void setUp() throws Exception {
        
        // setup services
        applicationContext = ApplicationContextHelper.getApplicationContext();
        nodeService = applicationContext.getBean("nodeService", NodeService.class);
        policyComponent = applicationContext.getBean("policyComponent", PolicyComponent.class);
        caseElementService = applicationContext.getBean("caseElementService", CaseElementServiceImpl.class);
        
        // authenticate
        AuthenticationUtil.setRunAsUserSystem();
        
        // bind special behaviours
        bindCaseElementAddBehaviour();
        bindCaseElementUpdateBehaviour();
        bindCaseElementRemoveBehaviour();
        
        // flush collectors
        flushAdditions();
        flushUpdates();
        flushRemovals();
        
        // create test nodes
        rootNode = createNode(
                nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), 
                ContentModel.TYPE_CONTAINER, 
                CaseElementServiceTest.class.getSimpleName());
        caseNode1 = createNode(rootNode, ContentModel.TYPE_FOLDER, "caseNode1");
        caseNode2 = createNode(rootNode, ContentModel.TYPE_FOLDER, "caseNode2");
        testNode1 = createNode(rootNode, ContentModel.TYPE_CONTENT, "testNode1");
        testNode2 = createNode(rootNode, ContentModel.TYPE_CONTENT, "testNode2");
        
        testConfig = caseElementService.getConfig(TEST_CONFIG_NAME);
        
        nodeService.addAspect(caseNode1, ICaseModel.ASPECT_CASE, null);
        nodeService.addAspect(caseNode2, ICaseModel.ASPECT_CASE, null);
    }*/
    
    private NodeRef createNode(NodeRef rootNode, QName type, String name) {
        return nodeService.createNode(
                rootNode, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(ICaseModel.NAMESPACE, name), 
                type).getChildRef();
    }

    @After
    public void tearDown() throws Exception {
        unbindCaseElementAddBehaviour();
        unbindCaseElementUpdateBehaviour();
        unbindCaseElementRemoveBehaviour();
        
        nodeService.addAspect(rootNode, ContentModel.ASPECT_TEMPORARY, null);
        nodeService.deleteNode(rootNode);
        
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Test
    public void testCaseElementBehaviours() {
        
        flushAdditions();
        caseElementService.addElement(testNode1, caseNode1, TEST_CONFIG_NAME);
        caseElementService.addElement(testNode2, caseNode2, TEST_CONFIG_NAME);
        assertEquals(
                createSet(new BehaviourCall(caseNode1, testNode1, testConfig),
                          new BehaviourCall(caseNode2, testNode2, testConfig)),
                additions);
        
        assertEquals(Collections.singletonList(caseNode1), caseElementService.getCases(testNode1, TEST_CONFIG_NAME));
        assertEquals(Collections.singletonList(caseNode2), caseElementService.getCases(testNode2, TEST_CONFIG_NAME));
        
        assertTrue(nodeService.hasAspect(testNode1, ICaseModel.ASPECT_ELEMENT));
        assertTrue(nodeService.hasAspect(testNode2, ICaseModel.ASPECT_ELEMENT));
        
        flushUpdates();
        nodeService.setProperty(testNode1, ContentModel.PROP_AUTHOR, "test");
        nodeService.setProperty(testNode2, ContentModel.PROP_AUTHOR, "test");
        assertEquals(
                createSet(new BehaviourCall(caseNode1, testNode1, testConfig),
                          new BehaviourCall(caseNode2, testNode2, testConfig)),
                updates);
        
        flushAdditions();
        caseElementService.addElement(testNode1, caseNode2, TEST_CONFIG_NAME);
        assertEquals(
                createSet(new BehaviourCall(caseNode2, testNode1, testConfig)),
                additions);
        
        flushUpdates();
        nodeService.setProperty(testNode1, ContentModel.PROP_AUTHOR, "test2");
        assertEquals(
                createSet(new BehaviourCall(caseNode1, testNode1, testConfig),
                          new BehaviourCall(caseNode2, testNode1, testConfig)),
                updates);
        
        // test removal by removing secondary child association:
        assertTrue(removals.isEmpty());
        caseElementService.removeElement(testNode2, caseNode2, TEST_CONFIG_NAME);
        assertEquals(
                createSet(new BehaviourCall(caseNode2, testNode2, testConfig)),
                removals);
        
        // test removal by deleting node:
        flushRemovals();
        nodeService.deleteNode(testNode1);
        assertEquals(
                createSet(new BehaviourCall(caseNode1, testNode1, testConfig),
                          new BehaviourCall(caseNode2, testNode1, testConfig)),
                removals);
    }

    @Override
    public void onCaseElementAdd(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        Log log = LogFactory.getLog(CaseElementServiceTest.class);
        log.info("Adding case element " + element + " to case " + caseRef + " (config " + config + ")");
        additions.add(new BehaviourCall(caseRef, element, config));
    }
    
    @Override
    public void onCaseElementUpdate(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        Log log = LogFactory.getLog(CaseElementServiceTest.class);
        log.info("Updating case element " + element + " in case " + caseRef + " (config " + config + ")");
        updates.add(new BehaviourCall(caseRef, element, config));
    }
    
    @Override
    public void onCaseElementRemove(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        Log log = LogFactory.getLog(CaseElementServiceTest.class);
        log.info("Removing case element " + element + " from case " + caseRef + " (config " + config + ")");
        removals.add(new BehaviourCall(caseRef, element, config));
    }

    private void bindCaseElementAddBehaviour() {
        caseElementAddDefinition = policyComponent.bindClassBehaviour(CaseElementPolicies.OnCaseElementAddPolicy.QNAME, ICaseModel.ASPECT_CASE, 
                new JavaBehaviour(this, "onCaseElementAdd"));
    }
    
    private void unbindCaseElementAddBehaviour() {
        if(caseElementAddDefinition != null) {
            policyComponent.removeClassDefinition(caseElementAddDefinition);
            caseElementAddDefinition = null;
        }
    }
    
    private void bindCaseElementUpdateBehaviour() {
        caseElementUpdateDefinition = policyComponent.bindClassBehaviour(CaseElementPolicies.OnCaseElementUpdatePolicy.QNAME, ICaseModel.ASPECT_CASE, 
                new JavaBehaviour(this, "onCaseElementUpdate"));
    }
    
    private void unbindCaseElementUpdateBehaviour() {
        if(caseElementUpdateDefinition != null) {
            policyComponent.removeClassDefinition(caseElementUpdateDefinition);
            caseElementUpdateDefinition = null;
        }
    }
    
    private void bindCaseElementRemoveBehaviour() {
        caseElementRemoveDefinition = policyComponent.bindClassBehaviour(CaseElementPolicies.OnCaseElementRemovePolicy.QNAME, ICaseModel.ASPECT_CASE, 
                new JavaBehaviour(this, "onCaseElementRemove"));
    }
    
    private void unbindCaseElementRemoveBehaviour() {
        if(caseElementRemoveDefinition != null) {
            policyComponent.removeClassDefinition(caseElementRemoveDefinition);
            caseElementRemoveDefinition = null;
        }
    }
    
    private void flushAdditions() {
        additions = new HashSet<>();
    }
    
    private void flushUpdates() {
        updates = new HashSet<>();
    }
    
    private void flushRemovals() {
        removals = new HashSet<>();
    }
    
    @SafeVarargs
    private static <E> Set<E> createSet(E... elements) {
        Set<E> result = new HashSet<>(elements.length);
        result.addAll(Arrays.asList(elements));
        return result;
    }
    
    private static final class BehaviourCall {
        private final NodeRef caseRef, element;
        private final ElementConfigDto config;
        
        public BehaviourCall(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
            this.caseRef = caseRef;
            this.element = element;
            this.config = config;
        }
        
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof BehaviourCall) {
                BehaviourCall that = (BehaviourCall) obj;
                return new EqualsBuilder()
                        .append(this.caseRef, that.caseRef)
                        .append(this.element, that.element)
                        .append(this.config,  that.config)
                        .isEquals();
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(caseRef)
                    .append(element)
                    .append(config)
                    .toHashCode();
        }
        
        @Override
        public String toString() {
            return "BehaviourCall[case=" +
                    caseRef +
                    ",element=" +
                    element +
                    ",config=" +
                    config +
                    "]";
        }
        
    }

}
