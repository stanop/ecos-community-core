package ru.citeck.ecos.action;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import ru.citeck.ecos.action.evaluator.ScriptEvaluator;
import ru.citeck.ecos.model.ConditionModel;
//import ru.citeck.ecos.test.ApplicationContextHelper;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(loader=ApplicationContextHelper.class)
@Transactional
public class ConditionDAOTest {

    private ApplicationContext applicationContext;
    private NodeService nodeService;
    private NodeRef testRoot, testNode;
    private ConditionDAO conditionDAO;
    private ActionService actionService;

    @Before
    public void setUp() throws Exception {
        
//        applicationContext = ApplicationContextHelper.getApplicationContext();
//        nodeService = applicationContext.getBean("nodeService", NodeService.class);
//        actionService = applicationContext.getBean("actionService", ActionService.class);
//        conditionDAO = applicationContext.getBean("EcoS.ConditionDAO", ConditionDAO.class);
//
//        AuthenticationUtil.setRunAsUserSystem();
//
//        NodeRef storeRoot = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
//        testRoot = nodeService.createNode(storeRoot,ContentModel.ASSOC_CHILDREN,
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
//                        ConditionDAOTest.class.getSimpleName()),
//                ContentModel.TYPE_CONTAINER).getChildRef();
//        testNode = nodeService.createNode(testRoot, ContentModel.ASSOC_CHILDREN,
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "test"),
//                ContentModel.TYPE_CONTENT).getChildRef();
       
    }
    
    @Test
    public void testComparePropertyValueCondition() {
        
//        QName propertyName = ContentModel.PROP_AUTHOR;
//        String value1 = "value1";
//        String value2 = "value2";
//
//        ActionCondition condition;
//        NodeRef conditionRef;
//
//        QName conditionType = ConditionModel.ComparePropertyValue.TYPE;
//        Map<QName, Serializable> actionProperties = new HashMap<>();
//        actionProperties.put(ConditionModel.ComparePropertyValue.PROP_PROPERTY, propertyName);
//        actionProperties.put(ConditionModel.ComparePropertyValue.PROP_OPERATION, ComparePropertyValueOperation.EQUALS);
//        actionProperties.put(ConditionModel.ComparePropertyValue.PROP_VALUE, value2);
//        conditionRef = nodeService.createNode(testRoot, ContentModel.ASSOC_CHILDREN,
//                conditionType, conditionType, actionProperties).getChildRef();
//
//        condition = conditionDAO.readCondition(conditionRef);
//
//        nodeService.setProperty(testNode, propertyName, value1);
//        assertFalse(actionService.evaluateActionCondition(condition, testNode));
//
//        nodeService.setProperty(testNode, propertyName, value2);
//        assertTrue(actionService.evaluateActionCondition(condition, testNode));
//
//        // create condition by action service, save, read and check
//        condition = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
//        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, propertyName);
//        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS);
//        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, value2);
//
//        conditionRef = conditionDAO.save(condition, testRoot, ContentModel.ASSOC_CHILDREN);
//        condition = conditionDAO.readCondition(conditionRef);
//
//        nodeService.setProperty(testNode, propertyName, value1);
//        assertFalse(actionService.evaluateActionCondition(condition, testNode));
//
//        nodeService.setProperty(testNode, propertyName, value2);
//        assertTrue(actionService.evaluateActionCondition(condition, testNode));
    }
    
    @Test
    public void testEvaluateScriptCondition() {
            
//        QName propertyName = ContentModel.PROP_AUTHOR;
//        String value1 = "value1";
//        String value2 = "value2";
//
//        ActionCondition condition;
//        NodeRef conditionRef;
//
//        condition = actionService.createActionCondition(ScriptEvaluator.NAME);
//        condition.setParameterValue(ScriptEvaluator.PARAM_SCRIPT, "document.properties['" + propertyName + "'] == '" + value2 + "'");
//
//        conditionRef = conditionDAO.save(condition, testRoot, ContentModel.ASSOC_CHILDREN);
//        condition = conditionDAO.readCondition(conditionRef);
//
//        nodeService.setProperty(testNode, propertyName, value1);
//        assertFalse(actionService.evaluateActionCondition(condition, testNode));
//
//        nodeService.setProperty(testNode, propertyName, value2);
//        assertTrue(actionService.evaluateActionCondition(condition, testNode));
        
    }
    
    
    @After
    public void tearDown() throws Exception {
//        nodeService.addAspect(testRoot, ContentModel.ASPECT_TEMPORARY, null);
//        nodeService.deleteNode(testRoot);
//
//        AuthenticationUtil.clearCurrentSecurityContext();
    }

}
