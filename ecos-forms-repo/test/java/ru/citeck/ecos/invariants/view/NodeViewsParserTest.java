package ru.citeck.ecos.invariants.view;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import ru.citeck.ecos.invariants.view.NodeField;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewElement;
import ru.citeck.ecos.invariants.view.NodeViewsParser;
//import ru.citeck.ecos.model.ClassificationModel;
//import ru.citeck.ecos.model.PassportModel;

@Ignore
public class NodeViewsParserTest {
    
    private InputStream viewsFile;
    private NodeViewsParser parser;

    @Before
    public void setUp() throws Exception {
        ClassPathResource resource = new ClassPathResource("alfresco/test/invariants/test-views.xml");
        viewsFile = resource.getInputStream();
        parser = new NodeViewsParser();
    }

    @After
    public void tearDown() throws Exception {
        if(viewsFile != null) {
            viewsFile.close();
        }
    }

/*    @Test
    public void testParse() {
        List<NodeViewElement> elements = parser.parse(viewsFile);
        assertEquals(1, elements.size());
        
        NodeViewElement element;
        NodeView view, childView;
        
        assertTrue(elements.get(0) instanceof NodeView);
        view = (NodeView) elements.get(0);
        elements = view.getElements();
        
//      <view class="pass:passport">
        assertView(view, PassportModel.TYPE_PASSPORT, null, true, false, null, null, null, 7);
        
        Iterator<NodeViewElement> iterator = elements.iterator();
        
//      <field prop="tk:type" />
        element = iterator.next();
        assertTrue(element instanceof NodeField);
        assertField((NodeField) element, ClassificationModel.PROP_DOCUMENT_TYPE, true, false, true, false, null, null, null, 0);
        
//      <field prop="tk:kind" />
        element = iterator.next();
        assertTrue(element instanceof NodeField);
        assertField((NodeField) element, ClassificationModel.PROP_DOCUMENT_KIND, true, false, true, false, null, null, null, 0);
        
//      <field assoc="pass:person" />
        element = iterator.next();
        assertTrue(element instanceof NodeField);
        assertField((NodeField) element, PassportModel.ASSOC_PERSON, false, true, true, false, null, null, null, 0);
        
//      <view template="rowset">
        element = iterator.next();
        assertTrue(element instanceof NodeView);
        childView = (NodeView) element;
        assertView(childView, null, null, true, false, null, null, "rowset", 6);
        
//      <fields any="true">
//        <invariant on="relevant" language="javascript">node.properties['tk:type'].nodeRef == "workspace://SpacesStore/idocs-cat-dockind-passport-rus"</invariant>
//      </fields>
        assertField((NodeField) childView.getElements().get(0), null, false, false, false, true, null, null, null, 0);
        
//      <field prop="pass:series" template="half-width" />
        assertField((NodeField) childView.getElements().get(1), PassportModel.PROP_SERIES, true, false, true, false, null, null, "half-width", 0);
        
//      <field prop="pass:number" template="half-width" />
        assertField((NodeField) childView.getElements().get(2), PassportModel.PROP_NUMBER, true, false, true, false, null, null, "half-width", 0);
        
//      <field prop="pass:issuingAuthority">
//        <region name="input" template="textarea">
//          <param name="width">95%</param>
//        </region>
//      </field>
        assertField((NodeField) childView.getElements().get(3), PassportModel.PROP_ISSUING_AUTHORITY, true, false, true, false, null, null, null, 1);
        
//      <field prop="pass:issueDate" template="half-width" />
        assertField((NodeField) childView.getElements().get(4), PassportModel.PROP_ISSUE_DATE, true, false, true, false, null, null, "half-width", 0);
        
//      <field prop="pass:subdivisionCode" template="half-width" />
        assertField((NodeField) childView.getElements().get(5), PassportModel.PROP_SUBDIVISION_CODE, true, false, true, false, null, null, "half-width", 0);
        
//      <view template="rowset">
        element = iterator.next();
        assertTrue(element instanceof NodeView);
        childView = (NodeView) element;
        assertView(childView, null, null, true, false, null, null, "rowset", 2);
        
//      <fields any="true">
//          <invariant on="relevant" language="javascript">node.properties['tk:type'].nodeRef == "workspace://SpacesStore/idocs-cat-dockind-passport-other"</invariant>
//      </fields>
        assertField((NodeField) childView.getElements().get(0), null, false, false, false, true, null, null, null, 0);
      
//        <field prop="pass:info">
//            <region name="input" template="textarea">
//                <param name="width">95%</param>
//            </region>
//        </field>
        assertField((NodeField) childView.getElements().get(1), PassportModel.PROP_INFO, true, false, true, false, null, null, null, 1);
        
//        <field prop="cm:content" kind="file-upload">
//            <invariant on="mandatory" language="explicit">true</invariant>
//        </field>
        element = iterator.next();
        assertTrue(element instanceof NodeField);
        assertField((NodeField) element, ContentModel.PROP_CONTENT, true, false, true, false, null, "file-upload", null, 0);
        
//      <field prop="privacy:consent" />
        element = iterator.next();
        assertTrue(element instanceof NodeField);
        assertField((NodeField) element, QName.createQName("http://www.citeck.ru/model/privacy/1.0", "consent"), true, false, true, false, null, null, null, 0);
        
    }*/

    private void assertElement(NodeViewElement element, boolean concrete, boolean any, String id, String kind, String template, int elementsCount) {
        assertEquals(any, element.any);
        assertEquals(concrete, element.isConcrete());
        assertEquals(id, element.getId());
        assertEquals(kind, element.getKind());
        assertEquals(template, element.getTemplate());
        assertEquals(elementsCount, element.getElements().size());
    }
    
    private void assertView(NodeView view, QName className, NodeViewMode mode, boolean concrete, boolean any, String id, String kind, String template, int elementsCount) {
        assertElement(view, concrete, any, id, kind, template, elementsCount);
        assertEquals(className, view.getClassName());
        assertEquals(mode, view.getMode());
    }
    
    private void assertField(NodeField field, QName attributeName, boolean isProperty, boolean isAssociation, boolean concrete, boolean any, String id, String kind, String template, int regionsCount) {
        assertElement(field, concrete, any, id, kind, template, regionsCount);
        assertEquals(attributeName, field.getAttributeName());
        assertEquals(isProperty, field.isProperty());
        assertEquals(isAssociation, field.isAssociation());
    }


}
