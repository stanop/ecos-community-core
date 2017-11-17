
package ru.citeck.ecos.journals.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import ru.citeck.ecos.invariants.xml.Invariant;


/**
 * <p>Java class for filter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="filter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="param" type="{http://www.citeck.ru/ecos/journals/1.0}option" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="region" type="{http://www.citeck.ru/ecos/journals/1.0}filterRegion" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="invariant" type="{http://www.citeck.ru/ecos/invariants/1.0}invariant" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="template" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "filter", propOrder = {
    "param",
    "region",
    "invariant"
})
public class Filter {

    protected List<Option> param;
    protected List<FilterRegion> region;
    protected List<Invariant> invariant;
    @XmlAttribute(name = "template")
    protected String template;

    /**
     * Gets the value of the param property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Option }
     * 
     * 
     */
    public List<Option> getParam() {
        if (param == null) {
            param = new ArrayList<Option>();
        }
        return this.param;
    }

    /**
     * Gets the value of the region property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the region property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FilterRegion }
     * 
     * 
     */
    public List<FilterRegion> getRegion() {
        if (region == null) {
            region = new ArrayList<FilterRegion>();
        }
        return this.region;
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
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplate(String value) {
        this.template = value;
    }

}
