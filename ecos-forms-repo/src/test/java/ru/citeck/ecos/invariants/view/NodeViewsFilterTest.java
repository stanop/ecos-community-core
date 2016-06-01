package ru.citeck.ecos.invariants.view;

//import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

//import org.alfresco.service.cmr.dictionary.DictionaryService;
//import org.alfresco.service.namespace.NamespaceService;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.io.ClassPathResource;

import ru.citeck.ecos.invariants.view.NodeField;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewElement;
import ru.citeck.ecos.invariants.view.NodeViewsFilter;
import ru.citeck.ecos.invariants.view.NodeViewsParser;
//import ru.citeck.ecos.model.ClassificationModel;
//import ru.citeck.ecos.model.PassportModel;
//import ru.citeck.ecos.test.ApplicationContextHelper;

public class NodeViewsFilterTest {
    
    private static final String TEST_SOURCE_ID = "test";
    
    private List<NodeViewElement> elements;
    private NodeViewsFilter filter;
//    private NamespaceService namespaceService;

//    @Before
//    public void setUp() throws Exception {
//        elements = parseTestElements();
//        filter = new NodeViewsFilter();
//        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
//        DictionaryService dictionaryService = applicationContext.getBean("dictionaryService", DictionaryService.class);
//        namespaceService = applicationContext.getBean("namespaceService", NamespaceService.class);
//        filter.setDictionaryService(dictionaryService);
//        filter.setPrefixResolver(namespaceService);
//
//        filter.registerViews(elements, TEST_SOURCE_ID);
//    }
//
//    private List<NodeViewElement> parseTestElements() {
//        NodeViewsParser parser = new NodeViewsParser();
//        ClassPathResource resource = new ClassPathResource("alfresco/test/invariants/test-views.xml");
//        InputStream viewsFile = null;
//        try {
//            viewsFile = resource.getInputStream();
//            return parser.parse(viewsFile);
//        } catch (IOException e) {
//            return null;
//        } finally {
//            if(viewsFile != null) {
//                try {
//                    viewsFile.close();
//                } catch (IOException e) {
//                    // do nothing
//                }
//            }
//        }
//    }
//
//    @After
//    public void tearDown() throws Exception {
//    }
//
//    @Test
//    public void testRegister() {
//        Collection<NodeViewElement> registeredElements = filter.getRegisteredViews(TEST_SOURCE_ID);
//        assertTrue(registeredElements != null);
//        assertEquals(elements.size(), registeredElements.size());
//
//        filter.unregisterViews(TEST_SOURCE_ID);
//        registeredElements = filter.getRegisteredViews(TEST_SOURCE_ID);
//        assertNull(registeredElements);
//    }
    
/*    @Test
    public void testSearch() {
        
        NodeView view = filter.resolveView(new NodeView.Builder(namespaceService)
                .className(PassportModel.TYPE_PASSPORT)
                .build());
        assertTrue(view != null);
        
        assertEquals(PassportModel.TYPE_PASSPORT, view.getClassName());
        assertEquals(null, view.getId());
        assertEquals(null, view.getKind());
        
        assertTrue(view.getElements() != null);
        assertEquals(7, view.getElements().size());
        
        assertTrue(view.getElements().get(0) instanceof NodeField);
        NodeField tkTypeField = (NodeField) view.getElements().get(0);
        assertEquals(ClassificationModel.PROP_DOCUMENT_TYPE, tkTypeField.getAttributeName());
        
    }*/
    
}
