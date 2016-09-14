
package ru.citeck.ecos.invariants.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scope complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scope">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.citeck.ru/ecos/invariants/1.0}scoped" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scope", propOrder = {
    "scoped"
})
@XmlSeeAlso({
    ClassScope.class,
    AttributeScope.class,
    AttributesScope.class
})
public class Scope {

    @XmlElements({
        @XmlElement(name = "aspect", type = Aspect.class),
        @XmlElement(name = "type", type = Type.class),
        @XmlElement(name = "child-associations", type = ChildAssociations.class),
        @XmlElement(name = "invariant", type = Invariant.class),
        @XmlElement(name = "property", type = Property.class),
        @XmlElement(name = "associations", type = Associations.class),
        @XmlElement(name = "properties", type = Properties.class),
        @XmlElement(name = "association", type = Association.class),
        @XmlElement(name = "child-association", type = ChildAssociation.class)
    })
    protected List<Object> scoped;

    /**
     * Gets the value of the scoped property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scoped property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScoped().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Aspect }
     * {@link Type }
     * {@link ChildAssociations }
     * {@link Invariant }
     * {@link Property }
     * {@link Associations }
     * {@link Properties }
     * {@link Association }
     * {@link ChildAssociation }
     * 
     * 
     */
    public List<Object> getScoped() {
        if (scoped == null) {
            scoped = new ArrayList<Object>();
        }
        return this.scoped;
    }

}
