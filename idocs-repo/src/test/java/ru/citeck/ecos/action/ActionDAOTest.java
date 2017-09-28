package ru.citeck.ecos.action;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
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

import ru.citeck.ecos.model.ActionModel;
//import ru.citeck.ecos.test.ApplicationContextHelper;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(loader=ApplicationContextHelper.class)
@Transactional
public class ActionDAOTest {

    private ApplicationContext applicationContext;
    private NodeService nodeService;
    private NodeRef testRoot, testNode;
    private ActionDAO actionDAO;
    private ActionService actionService;

    @Before
    public void setUp() throws Exception {
        
//        applicationContext = ApplicationContextHelper.getApplicationContext();
//        nodeService = applicationContext.getBean("nodeService", NodeService.class);
//        actionService = applicationContext.getBean("actionService", ActionService.class);
//        actionDAO = applicationContext.getBean("EcoS.ActionDAO", ActionDAO.class);
//
//        AuthenticationUtil.setRunAsUserSystem();
//
//        NodeRef storeRoot = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
//        testRoot = nodeService.createNode(storeRoot,ContentModel.ASSOC_CHILDREN,
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
//                        ActionDAOTest.class.getSimpleName()),
//                ContentModel.TYPE_CONTAINER).getChildRef();
//        testNode = nodeService.createNode(testRoot, ContentModel.ASSOC_CHILDREN,
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "test"),
//                ContentModel.TYPE_CONTENT).getChildRef();
       
    }
    
    @Test
    public void testSetPropertyValueAction() {
        
//        QName propertyName = ContentModel.PROP_AUTHOR;
//        String value1 = "value1";
//        String value2 = "value2";
//        Serializable actualValue;
//
//        Action action;
//        NodeRef actionRef;
//
//        QName actionType = ActionModel.SetPropertyValue.TYPE;
//        Map<QName, Serializable> actionProperties = new HashMap<>();
//        actionProperties.put(ActionModel.SetPropertyValue.PROP_PROPERTY, propertyName);
//        actionProperties.put(ActionModel.SetPropertyValue.PROP_VALUE, value2);
//        actionRef = nodeService.createNode(testRoot, ContentModel.ASSOC_CHILDREN,
//                actionType, actionType, actionProperties).getChildRef();
//
//        action = actionDAO.readAction(actionRef);
//
//        nodeService.setProperty(testNode, propertyName, value1);
//        actualValue = nodeService.getProperty(testNode, propertyName);
//        assertEquals(value1, actualValue);
//
//        actionService.executeAction(action, testNode);
//        actualValue = nodeService.getProperty(testNode, propertyName);
//        assertEquals(value2, actualValue);
//
//        // create action by action service, save, read and check
//        action = actionService.createAction(SetPropertyValueActionExecuter.NAME);
//        action.setParameterValue(SetPropertyValueActionExecuter.PARAM_PROPERTY, propertyName);
//        action.setParameterValue(SetPropertyValueActionExecuter.PARAM_VALUE, value2);
//
//        actionRef = actionDAO.save(action, testRoot, ContentModel.ASSOC_CHILDREN);
//        action = actionDAO.readAction(actionRef);
//
//        nodeService.setProperty(testNode, propertyName, value1);
//        actualValue = nodeService.getProperty(testNode, propertyName);
//        assertEquals(value1, actualValue);
//
//        actionService.executeAction(action, testNode);
//        actualValue = nodeService.getProperty(testNode, propertyName);
//        assertEquals(value2, actualValue);
        
    }
        
    @Test
    public void testExecuteScriptAction() {
        
//        QName propertyName = ContentModel.PROP_AUTHOR;
//        String value1 = "value1";
//        String value2 = "value2";
//        Serializable actualValue;
//
//        Action action;
//        NodeRef actionRef;
//
//        action = actionService.createAction(ScriptParamActionExecuter.NAME);
//        action.setParameterValue(ScriptParamActionExecuter.PARAM_SCRIPT, "document.properties['" + propertyName + "'] = '" + value2 + "'; document.save();");
//
//        actionRef = actionDAO.save(action, testRoot, ContentModel.ASSOC_CHILDREN);
//        action = actionDAO.readAction(actionRef);
//
//        nodeService.setProperty(testNode, propertyName, value1);
//        actualValue = nodeService.getProperty(testNode, propertyName);
//        assertEquals(value1, actualValue);
//
//        actionService.executeAction(action, testNode);
//        actualValue = nodeService.getProperty(testNode, propertyName);
//        assertEquals(value2, actualValue);
        
    }
    
    
    @After
    public void tearDown() throws Exception {
        nodeService.addAspect(testRoot, ContentModel.ASPECT_TEMPORARY, null);
        nodeService.deleteNode(testRoot);
        
        AuthenticationUtil.clearCurrentSecurityContext();
    }

}
