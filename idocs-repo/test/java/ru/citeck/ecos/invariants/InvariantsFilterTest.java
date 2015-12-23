package ru.citeck.ecos.invariants;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import ru.citeck.ecos.attr.NodeAttributeService;
import ru.citeck.ecos.invariants.attr.TargetAssocsAttributeType;
import ru.citeck.ecos.invariants.attr.VirtualAttributeType;
import ru.citeck.ecos.invariants.attr.PropertiesAttributeType;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.PassportModel;
import ru.citeck.ecos.test.ApplicationContextHelper;

public class InvariantsFilterTest {
    
    private static final String TEST_SOURCE_ID = "test";
    
    private ApplicationContext applicationContext;
    private List<InvariantDefinition> invariants;
    private InvariantsFilter filter;

    @Before
    public void setUp() throws Exception {
        invariants = parseTestInvariants();
        filter = new InvariantsFilter();
        applicationContext = ApplicationContextHelper.getApplicationContext();
        DictionaryService dictionaryService = applicationContext.getBean("dictionaryService", DictionaryService.class);
        filter.setDictionaryService(dictionaryService);
        filter.setNodeAttributeService(applicationContext.getBean("nodeAttributeService", NodeAttributeService.class));
        
        Map<QName, InvariantAttributeType> attributeTypes = new HashMap<>();
        attributeTypes.put(AttributeModel.TYPE_VIRTUAL, initAttributeType(new VirtualAttributeType()));
        attributeTypes.put(AttributeModel.TYPE_PROPERTY, initAttributeType(new PropertiesAttributeType()));
        attributeTypes.put(AttributeModel.TYPE_TARGET_ASSOCIATION, initAttributeType(new TargetAssocsAttributeType()));
        filter.setAttributeTypesRegistry(attributeTypes);
        
        filter.registerInvariants(invariants, TEST_SOURCE_ID);
    }

    private InvariantAttributeType initAttributeType(AbstractInvariantAttributeType attributeType) {
        attributeType.setDictionaryService(applicationContext.getBean("dictionaryService", DictionaryService.class));
        attributeType.setPrefixResolver(applicationContext.getBean("namespaceService", NamespaceService.class));
        attributeType.setMessageLookup(applicationContext.getBean("messageService", MessageService.class));
        attributeType.setNodeAttributeService(applicationContext.getBean("nodeAttributeService", NodeAttributeService.class));
        return attributeType;
    }

    private List<InvariantDefinition> parseTestInvariants() throws IOException {
        InvariantsParser parser = new InvariantsParser();
        ClassPathResource resource = new ClassPathResource("alfresco/test/invariants/test-invariants.xml");
        InputStream invariantsFile = resource.getInputStream();
        try {
            return parser.parse(invariantsFile, InvariantPriority.CUSTOM);
        } finally {
            if(invariantsFile != null) {
                invariantsFile.close();
            }
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRegister() {
        List<InvariantDefinition> registeredInvariants = filter.getInvariants(TEST_SOURCE_ID);
        assertTrue(registeredInvariants != null);
        assertEquals(invariants.size(), registeredInvariants.size());
        
        filter.unregisterInvariants(TEST_SOURCE_ID);
        registeredInvariants = filter.getInvariants(TEST_SOURCE_ID);
        assertTrue(registeredInvariants == null);
    }
    
    @Test
    public void testSearch() {
        
        // pass:passport - test search
        List<InvariantDefinition> foundInvariants = filter.searchMatchingInvariants(Collections.singletonList(PassportModel.TYPE_PASSPORT), false);
        assertEquals(27, foundInvariants.size());
        assertEquals(27, new HashSet<>(foundInvariants).size());
        
        // pass:passport - test ordering
        int lastLevel = 0;
        for(InvariantDefinition invariant : foundInvariants) {
            QName classScope = invariant.getClassScope();
            int level = classScope == null ? 3 : 
                        classScope.equals(ClassificationModel.ASPECT_DOCUMENT_TYPE_KIND) ? 2 : 
                        classScope.equals(PassportModel.TYPE_PASSPORT) ? 1 : 
                        0;
            assertTrue(level >= lastLevel);
            lastLevel = level;
        }
        
        // tk:documentTypeKind - test search & ordering
        foundInvariants = filter.searchMatchingInvariants(Collections.singletonList(ClassificationModel.ASPECT_DOCUMENT_TYPE_KIND), false);
        assertEquals(3, foundInvariants.size());
        assertEquals(3, new HashSet<>(foundInvariants).size());
        assertEquals(ClassificationModel.ASPECT_DOCUMENT_TYPE_KIND, foundInvariants.get(0).getClassScope());
        assertNull(foundInvariants.get(1).getClassScope());
        assertNull(foundInvariants.get(2).getClassScope());
        
    }
    
}
