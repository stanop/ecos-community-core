
package ru.citeck.ecos.invariants.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.citeck.ecos.invariants.xml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Views_QNAME = new QName("http://www.citeck.ru/ecos/views/1.0", "views");
    private final static QName _InvariantItem_QNAME = new QName("http://www.citeck.ru/ecos/invariants/1.0", "item");
    private final static QName _InvariantCriterion_QNAME = new QName("http://www.citeck.ru/ecos/invariants/1.0", "criterion");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.citeck.ecos.invariants.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Association }
     * 
     */
    public Association createAssociation() {
        return new Association();
    }

    /**
     * Create an instance of {@link Aspect }
     * 
     */
    public Aspect createAspect() {
        return new Aspect();
    }

    /**
     * Create an instance of {@link Criterion }
     * 
     */
    public Criterion createCriterion() {
        return new Criterion();
    }

    /**
     * Create an instance of {@link Region }
     * 
     */
    public Region createRegion() {
        return new Region();
    }

    /**
     * Create an instance of {@link AttributeScope }
     * 
     */
    public AttributeScope createAttributeScope() {
        return new AttributeScope();
    }

    /**
     * Create an instance of {@link ClassScope }
     * 
     */
    public ClassScope createClassScope() {
        return new ClassScope();
    }

    /**
     * Create an instance of {@link View }
     * 
     */
    public View createView() {
        return new View();
    }

    /**
     * Create an instance of {@link Fields }
     * 
     */
    public Fields createFields() {
        return new Fields();
    }

    /**
     * Create an instance of {@link Field }
     * 
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link ViewRoot.Imports.Import }
     * 
     */
    public ViewRoot.Imports.Import createViewRootImportsImport() {
        return new ViewRoot.Imports.Import();
    }

    /**
     * Create an instance of {@link Properties }
     * 
     */
    public Properties createProperties() {
        return new Properties();
    }

    /**
     * Create an instance of {@link Invariants.Imports }
     * 
     */
    public Invariants.Imports createInvariantsImports() {
        return new Invariants.Imports();
    }

    /**
     * Create an instance of {@link ChildAssociations }
     * 
     */
    public ChildAssociations createChildAssociations() {
        return new ChildAssociations();
    }

    /**
     * Create an instance of {@link ViewRoot.Imports }
     * 
     */
    public ViewRoot.Imports createViewRootImports() {
        return new ViewRoot.Imports();
    }

    /**
     * Create an instance of {@link ViewRoot }
     * 
     */
    public ViewRoot createViewRoot() {
        return new ViewRoot();
    }

    /**
     * Create an instance of {@link Invariants }
     * 
     */
    public Invariants createInvariants() {
        return new Invariants();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link AttributesScope }
     * 
     */
    public AttributesScope createAttributesScope() {
        return new AttributesScope();
    }

    /**
     * Create an instance of {@link Type }
     * 
     */
    public Type createType() {
        return new Type();
    }

    /**
     * Create an instance of {@link Param }
     * 
     */
    public Param createParam() {
        return new Param();
    }

    /**
     * Create an instance of {@link Invariants.Imports.Import }
     * 
     */
    public Invariants.Imports.Import createInvariantsImportsImport() {
        return new Invariants.Imports.Import();
    }

    /**
     * Create an instance of {@link ChildAssociation }
     * 
     */
    public ChildAssociation createChildAssociation() {
        return new ChildAssociation();
    }

    /**
     * Create an instance of {@link Element }
     * 
     */
    public Element createElement() {
        return new Element();
    }

    /**
     * Create an instance of {@link Associations }
     * 
     */
    public Associations createAssociations() {
        return new Associations();
    }

    /**
     * Create an instance of {@link Views }
     * 
     */
    public Views createViews() {
        return new Views();
    }

    /**
     * Create an instance of {@link Regions }
     * 
     */
    public Regions createRegions() {
        return new Regions();
    }

    /**
     * Create an instance of {@link Scope }
     * 
     */
    public Scope createScope() {
        return new Scope();
    }

    /**
     * Create an instance of {@link Invariant }
     * 
     */
    public Invariant createInvariant() {
        return new Invariant();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ViewRoot }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.citeck.ru/ecos/views/1.0", name = "views")
    public JAXBElement<ViewRoot> createViews(ViewRoot value) {
        return new JAXBElement<ViewRoot>(_Views_QNAME, ViewRoot.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.citeck.ru/ecos/invariants/1.0", name = "item", scope = Invariant.class)
    public JAXBElement<String> createInvariantItem(String value) {
        return new JAXBElement<String>(_InvariantItem_QNAME, String.class, Invariant.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Criterion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.citeck.ru/ecos/invariants/1.0", name = "criterion", scope = Invariant.class)
    public JAXBElement<Criterion> createInvariantCriterion(Criterion value) {
        return new JAXBElement<Criterion>(_InvariantCriterion_QNAME, Criterion.class, Invariant.class, value);
    }

}
