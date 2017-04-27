package ru.citeck.ecos.invariants;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import ru.citeck.ecos.invariants.InvariantScope.AttributeScopeKind;
import ru.citeck.ecos.invariants.InvariantScope.ClassScopeKind;
//import ru.citeck.ecos.model.ClassificationModel;
//import ru.citeck.ecos.model.PassportModel;
import ru.citeck.ecos.search.SearchCriteria;

@Ignore
public class InvariantsParserTest {
    
    private InputStream invariantsFile;
    private InvariantsParser parser;

    @Before
    public void setUp() throws Exception {
        ClassPathResource resource = new ClassPathResource("alfresco/test/invariants/test-invariants.xml");
        invariantsFile = resource.getInputStream();
        parser = new InvariantsParser();
    }

    @After
    public void tearDown() throws Exception {
        if(invariantsFile != null) {
            invariantsFile.close();
        }
    }

/*    @Test
    public void testParse() {
        List<InvariantDefinition> invariants = parser.parse(invariantsFile, InvariantPriority.CUSTOM);
        assertEquals(27, invariants.size());
        
        InvariantDefinition invariant;
        SearchCriteria criteria;
        Iterator<InvariantDefinition> iterator = invariants.iterator();
        
//        <invariant on="options" language="criteria">
//            <criterion attribute="type" predicate="type-equals" value="cm:category" />
//            <criterion attribute="parent" predicate="parent-equals" value="workspace://SpacesStore/category-document-type-root" />
//        </invariant>
        invariant = iterator.next();
        assertEquals(ClassificationModel.ASPECT_DOCUMENT_TYPE_KIND, invariant.getClassScope());
        assertEquals(ClassScopeKind.ASPECT, invariant.getClassScopeKind());
        assertEquals(ClassificationModel.PROP_DOCUMENT_TYPE, invariant.getAttributeScope());
        assertEquals(AttributeScopeKind.PROPERTY, invariant.getAttributeScopeKind());
        assertEquals(Feature.OPTIONS, invariant.getFeature());
        assertEquals("criteria", invariant.getLanguage());
        assertTrue(invariant.getValue() instanceof SearchCriteria);
        criteria = (SearchCriteria) invariant.getValue();
        assertEquals(2, criteria.getTriplets().size());
        
//        <invariant on="options" language="criteria">
//            <criterion attribute="type" predicate="type-equals" value="cm:category" />
//            <criterion attribute="parent" predicate="parent-equals" value="${node.properties['tk:type'].nodeRef}" />
//        </invariant>
        invariant = iterator.next();
        assertNull(invariant.getClassScope());
        assertNull(invariant.getClassScopeKind());
        assertEquals(ClassificationModel.PROP_DOCUMENT_KIND, invariant.getAttributeScope());
        assertEquals(AttributeScopeKind.PROPERTY, invariant.getAttributeScopeKind());
        assertEquals(Feature.OPTIONS, invariant.getFeature());
        assertEquals("criteria", invariant.getLanguage());
        assertTrue(invariant.getValue() instanceof SearchCriteria);
        criteria = (SearchCriteria) invariant.getValue();
        assertEquals(2, criteria.getTriplets().size());
        
//        <invariant on="relevant" language="javascript">node.properties["tk:type"] != null</invariant>
        invariant = iterator.next();
        assertNull(invariant.getClassScope());
        assertNull(invariant.getClassScopeKind());
        assertEquals(ClassificationModel.PROP_DOCUMENT_KIND, invariant.getAttributeScope());
        assertEquals(AttributeScopeKind.PROPERTY, invariant.getAttributeScopeKind());
        assertEquals(Feature.RELEVANT, invariant.getFeature());
        assertEquals("javascript", invariant.getLanguage());
        assertTrue(invariant.getExpression() != null);
        
//        <invariant on="title" language="javascript">"Вид договора"</invariant>
        invariant = iterator.next();
        assertEquals(PassportModel.TYPE_PASSPORT, invariant.getClassScope());
        assertEquals(ClassScopeKind.TYPE, invariant.getClassScopeKind());
        assertEquals(ClassificationModel.PROP_DOCUMENT_KIND, invariant.getAttributeScope());
        assertEquals(AttributeScopeKind.PROPERTY, invariant.getAttributeScopeKind());
        assertEquals(Feature.TITLE, invariant.getFeature());
        assertEquals("javascript", invariant.getLanguage());
        assertTrue(invariant.getExpression() != null);
        
//        <invariant on="description" language="freemarker">Вид договора</invariant>
        invariant = iterator.next();
        assertEquals(PassportModel.TYPE_PASSPORT, invariant.getClassScope());
        assertEquals(ClassScopeKind.TYPE, invariant.getClassScopeKind());
        assertEquals(ClassificationModel.PROP_DOCUMENT_KIND, invariant.getAttributeScope());
        assertEquals(AttributeScopeKind.PROPERTY, invariant.getAttributeScopeKind());
        assertEquals(Feature.DESCRIPTION, invariant.getFeature());
        assertEquals("freemarker", invariant.getLanguage());
        assertTrue(invariant.getExpression() != null);
        
//        <invariant on="value" language="explicit">workspace://SpacesStore/idocs-cat-doctype-passport</invariant>
        invariant = iterator.next();
        assertEquals(PassportModel.TYPE_PASSPORT, invariant.getClassScope());
        assertEquals(ClassScopeKind.TYPE, invariant.getClassScopeKind());
        assertEquals(ClassificationModel.PROP_DOCUMENT_TYPE, invariant.getAttributeScope());
        assertEquals(AttributeScopeKind.PROPERTY, invariant.getAttributeScopeKind());
        assertEquals(Feature.VALUE, invariant.getFeature());
        assertEquals("explicit", invariant.getLanguage());
        assertEquals("workspace://SpacesStore/idocs-cat-doctype-passport", invariant.getExpression());
        assertEquals("workspace://SpacesStore/idocs-cat-doctype-passport", invariant.getValue());
        
//        <invariant on="mandatory" language="explicit">true</invariant>
        invariant = iterator.next();
        assertEquals(PassportModel.TYPE_PASSPORT, invariant.getClassScope());
        assertEquals(ClassScopeKind.TYPE, invariant.getClassScopeKind());
        assertEquals(ClassificationModel.PROP_DOCUMENT_KIND, invariant.getAttributeScope());
        assertEquals(AttributeScopeKind.PROPERTY, invariant.getAttributeScopeKind());
        assertEquals(Feature.MANDATORY, invariant.getFeature());
        assertEquals("explicit", invariant.getLanguage());
        assertEquals("true", invariant.getExpression());
    }*/

}
