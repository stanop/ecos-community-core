
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
 * <p>Java class for field complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="field">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.citeck.ru/ecos/views/1.0}element">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="region" type="{http://www.citeck.ru/ecos/views/1.0}region"/>
 *           &lt;element name="regions" type="{http://www.citeck.ru/ecos/views/1.0}regions"/>
 *         &lt;/choice>
 *         &lt;element name="invariant" type="{http://www.citeck.ru/ecos/invariants/1.0}invariant" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="prop" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="assoc" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="property" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="association" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "field", namespace = "http://www.citeck.ru/ecos/views/1.0", propOrder = {
    "regionOrRegions",
    "invariant"
})
@XmlSeeAlso({
    Fields.class
})
public class Field
    extends Element
{

    @XmlElements({
        @XmlElement(name = "region"),
        @XmlElement(name = "regions", type = Regions.class)
    })
    protected List<Region> regionOrRegions;
    protected List<Invariant> invariant;
    @XmlAttribute
    protected String prop;
    @XmlAttribute
    protected String assoc;
    @XmlAttribute
    protected String property;
    @XmlAttribute
    protected String association;

    /**
     * Gets the value of the regionOrRegions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the regionOrRegions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegionOrRegions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Region }
     * {@link Regions }
     * 
     * 
     */
    public List<Region> getRegionOrRegions() {
        if (regionOrRegions == null) {
            regionOrRegions = new ArrayList<Region>();
        }
        return this.regionOrRegions;
    }

    /**
     * Gets the value of the invariant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the invariant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvariant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Invariant }
     * 
     * 
     */
    public List<Invariant> getInvariant() {
        if (invariant == null) {
            invariant = new ArrayList<Invariant>();
        }
        return this.invariant;
    }

    /**
     * Gets the value of the prop property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProp() {
        return prop;
    }

    /**
     * Sets the value of the prop property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProp(String value) {
        this.prop = value;
    }

    /**
     * Gets the value of the assoc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssoc() {
        return assoc;
    }

    /**
     * Sets the value of the assoc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssoc(String value) {
        this.assoc = value;
    }

    /**
     * Gets the value of the property property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProperty() {
        return property;
    }

    /**
     * Sets the value of the property property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProperty(String value) {
        this.property = value;
    }

    /**
     * Gets the value of the association property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssociation() {
        return association;
    }

    /**
     * Sets the value of the association property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssociation(String value) {
        this.association = value;
    }

}
