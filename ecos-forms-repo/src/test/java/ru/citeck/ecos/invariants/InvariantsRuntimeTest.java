package ru.citeck.ecos.invariants;

//import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.alfresco.model.ContentModel;
//import org.alfresco.repo.i18n.MessageService;
//import org.alfresco.repo.policy.BehaviourFilter;
//import org.alfresco.repo.security.authentication.AuthenticationUtil;
//import org.alfresco.service.ServiceRegistry;
//import org.alfresco.service.cmr.dictionary.DictionaryService;
//import org.alfresco.service.cmr.repository.NodeRef;
//import org.alfresco.service.cmr.repository.NodeService;
//import org.alfresco.service.cmr.repository.ScriptService;
//import org.alfresco.service.cmr.repository.StoreRef;
//import org.alfresco.service.cmr.repository.TemplateService;
//import org.alfresco.service.namespace.NamespaceService;
//import org.alfresco.service.namespace.QName;
//import org.alfresco.util.Pair;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.transaction.annotation.Transactional;

//import ru.citeck.ecos.attr.NodeAttributeService;
import ru.citeck.ecos.invariants.attr.PropertiesAttributeType;
import ru.citeck.ecos.invariants.attr.TargetAssocsAttributeType;
import ru.citeck.ecos.invariants.attr.VirtualAttributeType;
import ru.citeck.ecos.invariants.lang.*;
//import ru.citeck.ecos.model.AttributeModel;
//import ru.citeck.ecos.model.PassportModel;
//import ru.citeck.ecos.model.PrivacyModel;
//import ru.citeck.ecos.search.CriteriaSearchService;
//import ru.citeck.ecos.service.CiteckServices;
//import ru.citeck.ecos.test.ApplicationContextHelper;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(loader=ApplicationContextHelper.class)
//@Transactional
public class InvariantsRuntimeTest {
    
    private static final String TEST_SOURCE_ID = "test"; 
    
    private InvariantsRuntime runtime;
//    private ApplicationContext applicationContext;
//    private ServiceRegistry serviceRegistry;
//    private NodeService nodeService;
//
//    private List<InvariantDefinition> invariants;
//    private NodeRef testNode;
    
/*    @Before
    public void setUp() throws Exception {
        applicationContext = ApplicationContextHelper.getApplicationContext();
        serviceRegistry = applicationContext.getBean("ServiceRegistry", ServiceRegistry.class);
        nodeService = serviceRegistry.getNodeService();
        BehaviourFilter behaviourFilter = applicationContext.getBean("policyBehaviourFilter", BehaviourFilter.class);
        
        AuthenticationUtil.setRunAsUserSystem();
        behaviourFilter.disableBehaviour();
        
        invariants = getTestInvariants();
        runtime = createInvariantsRuntime();
        testNode = createTestNode();
    }*/

/*    private NodeRef createTestNode() {
        NodeRef testNode = nodeService.createNode(
                nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, this.getClass().getName()), 
                PassportModel.TYPE_PASSPORT).getChildRef();
        NodeRef admin = serviceRegistry.getPersonService().getPerson("admin");
        nodeService.createAssociation(testNode, admin, PassportModel.ASSOC_PERSON);
        nodeService.setProperty(testNode, PrivacyModel.PROP_CONSENT, true);
        return testNode;
    }

    private List<InvariantDefinition> getTestInvariants() throws IOException {
        InvariantsParser parser = new InvariantsParser();
        ClassPathResource resource = new ClassPathResource("alfresco/test/invariants/test-invariants.xml");
        InputStream invariantsFile = resource.getInputStream();
        List<InvariantDefinition> parsedInvariants;
        try {
            parsedInvariants = parser.parse(invariantsFile, InvariantPriority.CUSTOM);
        } finally {
            if(invariantsFile != null) {
                invariantsFile.close();
            }
        }
        
        InvariantsFilter filter = new InvariantsFilter();
        filter.setDictionaryService(serviceRegistry.getDictionaryService());
        filter.setNodeAttributeService((NodeAttributeService) serviceRegistry.getService(CiteckServices.NODE_ATTRIBUTE_SERVICE));
        Map<QName, InvariantAttributeType> attributeTypes = new HashMap<>();
        attributeTypes.put(AttributeModel.TYPE_VIRTUAL, initAttributeType(new VirtualAttributeType()));
        attributeTypes.put(AttributeModel.TYPE_PROPERTY, initAttributeType(new PropertiesAttributeType()));
        attributeTypes.put(AttributeModel.TYPE_TARGET_ASSOCIATION, initAttributeType(new TargetAssocsAttributeType()));
        filter.setAttributeTypesRegistry(attributeTypes);
        
        filter.registerInvariants(parsedInvariants, TEST_SOURCE_ID);
        return filter.searchMatchingInvariants(Collections.singleton(PassportModel.TYPE_PASSPORT));
    }*/
    
//    private InvariantAttributeType initAttributeType(AbstractInvariantAttributeType attributeType) {
//        attributeType.setDictionaryService(applicationContext.getBean("dictionaryService", DictionaryService.class));
//        attributeType.setPrefixResolver(applicationContext.getBean("namespaceService", NamespaceService.class));
//        attributeType.setMessageLookup(applicationContext.getBean("messageService", MessageService.class));
//        attributeType.setNodeAttributeService(applicationContext.getBean("nodeAttributeService", NodeAttributeService.class));
//        return attributeType;
//    }
//
//    private InvariantsRuntime createInvariantsRuntime() {
//        InvariantsRuntime runtime = new InvariantsRuntime();
//        runtime.setNodeAttributeService((NodeAttributeService) serviceRegistry.getService(CiteckServices.NODE_ATTRIBUTE_SERVICE));
//        registerLanguages(runtime);
//
//        Map<QName, InvariantAttributeType> attributeTypes = new HashMap<>();
//        attributeTypes.put(AttributeModel.TYPE_VIRTUAL, initAttributeType(new VirtualAttributeType()));
//        attributeTypes.put(AttributeModel.TYPE_PROPERTY, initAttributeType(new PropertiesAttributeType()));
//        attributeTypes.put(AttributeModel.TYPE_TARGET_ASSOCIATION, initAttributeType(new TargetAssocsAttributeType()));
//        runtime.setAttributeTypesRegistry(attributeTypes);
//
//        return runtime;
//    }
//
//    private void registerLanguages(InvariantsRuntime runtime) {
//
//        final Map<String, InvariantLanguage> registry = new HashMap<>();
//        runtime.setLanguagesRegistry(registry);
//
//        JavaScriptLanguage jslang = new JavaScriptLanguage();
//        jslang.setServiceRegistry(serviceRegistry);
//        jslang.setRegistry(registry);
//        jslang.setScriptService(applicationContext.getBean("scriptService", ScriptService.class));
//        jslang.init();
//
//        FreeMarkerLanguage fmlang = new FreeMarkerLanguage();
//        fmlang.setServiceRegistry(serviceRegistry);
//        fmlang.setRegistry(registry);
//        fmlang.setTemplateService(applicationContext.getBean("templateService", TemplateService.class));
//        fmlang.init();
//
//        CriteriaLanguage crlang = new CriteriaLanguage();
//        crlang.setServiceRegistry(serviceRegistry);
//        crlang.setRegistry(registry);
//        crlang.setCriteriaSearchService(applicationContext.getBean(CiteckServices.CRITERIA_SEARCH_SERVICE.getLocalName(), CriteriaSearchService.class));
//        crlang.setNamespaceService(serviceRegistry.getNamespaceService());
//        crlang.setTemplateService(applicationContext.getBean("templateService", TemplateService.class));
//        crlang.init();
//
//        ExplicitLanguage exlang = new ExplicitLanguage();
//        exlang.setServiceRegistry(serviceRegistry);
//        exlang.setRegistry(registry);
//        exlang.init();
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        nodeService.addAspect(testNode, ContentModel.ASPECT_TEMPORARY, null);
//        nodeService.deleteNode(testNode);
//
//        AuthenticationUtil.clearCurrentSecurityContext();
//    }

//    @Test
//    public void testExecute() {
//        
//        runtime.executeInvariants(testNode, invariants);
//    }
    
/*    @Test
    public void testExecute() {
        
        // test with correct attributes:
        Map<QName, Serializable> correctAttributes = new HashMap<>();
        correctAttributes.put(PassportModel.PROP_SERIES, "1234");
        correctAttributes.put(PassportModel.PROP_NUMBER, "123456");
        correctAttributes.put(PassportModel.PROP_ISSUING_AUTHORITY, "test issuing authority");
        correctAttributes.put(PassportModel.PROP_ISSUE_DATE, new Date());
        correctAttributes.put(PassportModel.PROP_SUBDIVISION_CODE, "123-456");
        correctAttributes.put(ContentModel.PROP_CONTENT, "test content");
        
        nodeService.setProperties(testNode, correctAttributes);
        runtime.executeInvariants(testNode, invariants);
        
        // test with unset mandatory attributes:
        Set<QName> mandatoryAttributes = new HashSet<>();
        mandatoryAttributes.add(PassportModel.PROP_SERIES);
        mandatoryAttributes.add(PassportModel.PROP_NUMBER);
        mandatoryAttributes.add(PassportModel.PROP_ISSUING_AUTHORITY);
        mandatoryAttributes.add(PassportModel.PROP_ISSUE_DATE);
        mandatoryAttributes.add(PassportModel.PROP_SUBDIVISION_CODE);
        mandatoryAttributes.add(ContentModel.PROP_CONTENT);
        
        // test with removing attribute:
        for(QName mandatoryAttribute : mandatoryAttributes) {
            boolean failed = false;
            try {
                nodeService.setProperties(testNode, correctAttributes);
                nodeService.removeProperty(testNode, mandatoryAttribute);
                runtime.executeInvariants(testNode, invariants);
            } catch(InvariantValidationException e) {
                failed = true;
                assertEquals(mandatoryAttribute, e.getAttributeName());
                assertEquals(Feature.MANDATORY, e.getViolatedInvariant().getFeature());
            }
            assertTrue(failed);
        }
        
        // test with nulling attribute:
        for(QName mandatoryAttribute : mandatoryAttributes) {
            boolean failed = false;
            try {
                nodeService.setProperties(testNode, correctAttributes);
                nodeService.setProperty(testNode, mandatoryAttribute, null);
                runtime.executeInvariants(testNode, invariants);
            } catch(InvariantValidationException e) {
                failed = true;
                assertEquals(mandatoryAttribute, e.getAttributeName());
                assertEquals(Feature.MANDATORY, e.getViolatedInvariant().getFeature());
            }
            assertTrue(failed);
        }
        
        // test with incorrect values:
        Set<Pair<QName, String>> incorrectValues = new HashSet<>();
        incorrectValues.add(new Pair<>(PassportModel.PROP_SERIES, "123"));
        incorrectValues.add(new Pair<>(PassportModel.PROP_SERIES, "123d"));
        incorrectValues.add(new Pair<>(PassportModel.PROP_SERIES, "abcd"));
        incorrectValues.add(new Pair<>(PassportModel.PROP_NUMBER, "123b"));
        incorrectValues.add(new Pair<>(PassportModel.PROP_NUMBER, "abcd"));
        for(Pair<QName, String> incorrectValue : incorrectValues) {
            QName attributeName = incorrectValue.getFirst();
            String attributeValue = incorrectValue.getSecond();
            boolean failed = false;
            try {
                nodeService.setProperties(testNode, correctAttributes);
                nodeService.setProperty(testNode, attributeName, attributeValue);
                runtime.executeInvariants(testNode, invariants);
            } catch(InvariantValidationException e) {
                failed = true;
                assertEquals(attributeName, e.getAttributeName());
                assertEquals(Feature.VALID, e.getViolatedInvariant().getFeature());
            }
            assertTrue(failed);
        }
            
        
        
        
    }*/
    
}
