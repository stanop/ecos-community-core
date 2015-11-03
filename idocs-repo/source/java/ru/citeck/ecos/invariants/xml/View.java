
package ru.citeck.ecos.invariants.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for view complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="view">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.citeck.ru/ecos/views/1.0}element">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.citeck.ru/ecos/views/1.0}elements" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="class" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mode" type="{http://www.citeck.ru/ecos/views/1.0}mode" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "view", namespace = "http://www.citeck.ru/ecos/views/1.0", propOrder = {
    "elements"
})
@XmlSeeAlso({
    Views.class
})
public class View
    extends Element
{

    @XmlElements({
        @XmlElement(name = "view", type = View.class),
        @XmlElement(name = "views", type = Views.class),
        @XmlElement(name = "region", type = Region.class),
        @XmlElement(name = "fields", type = Fields.class),
        @XmlElement(name = "field", type = Field.class),
        @XmlElement(name = "regions", type = Regions.class)
    })
    protected List<Element> elements;
    @XmlAttribute(name = "class")
    protected String clazz;
    @XmlAttribute
    protected Mode mode;

    /**
     * Gets the value of the elements property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the elements property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getElements().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link View }
     * {@link Views }
     * {@link Region }
     * {@link Fields }
     * {@link Field }
     * {@link Regions }
     * 
     * 
     */
    public List<Element> getElements() {
        if (elements == null) {
            elements = new ArrayList<Element>();
        }
        return this.elements;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link Mode }
     *     
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mode }
     *     
     */
    public void setMode(Mode value) {
        this.mode = value;
    }

}
