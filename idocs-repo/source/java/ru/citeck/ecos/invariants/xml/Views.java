
package ru.citeck.ecos.invariants.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for views complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="views">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.citeck.ru/ecos/views/1.0}view">
 *       &lt;attribute name="any" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "views", namespace = "http://www.citeck.ru/ecos/views/1.0")
public class Views
    extends View
{

    @XmlAttribute
    protected Boolean any;

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

}
