
package ru.citeck.ecos.invariants.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fields complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fields">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.citeck.ru/ecos/views/1.0}field">
 *       &lt;attribute name="any" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="datatype" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="nodetype" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fields", namespace = "http://www.citeck.ru/ecos/views/1.0")
public class Fields
    extends Field
{

    @XmlAttribute
    protected Boolean any;
    @XmlAttribute
    protected String datatype;
    @XmlAttribute
    protected String nodetype;

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAny() {
        if (any == null) {
            return false;
        } else {
            return any;
        }
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAny(Boolean value) {
        this.any = value;
    }

    /**
     * Gets the value of the datatype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * Sets the value of the datatype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatatype(String value) {
        this.datatype = value;
    }

    /**
     * Gets the value of the nodetype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNodetype() {
        return nodetype;
    }

    /**
     * Sets the value of the nodetype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNodetype(String value) {
        this.nodetype = value;
    }

}
