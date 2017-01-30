
package ru.citeck.ecos.journals.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for evaluator complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="evaluator">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="param" type="{http://www.citeck.ru/ecos/journals/1.0}option" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="evaluator" type="{http://www.citeck.ru/ecos/journals/1.0}evaluator" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="joinBy" type="{http://www.citeck.ru/ecos/journals/1.0}logicJoinType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "evaluator", propOrder = {
    "param",
    "evaluator"
})
public class Evaluator {

    protected List<Option> param;
    protected List<Evaluator> evaluator;
    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "joinBy")
    protected LogicJoinType joinBy;

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
     * Gets the value of the evaluator property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the evaluator property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEvaluator().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Evaluator }
     * 
     * 
     */
    public List<Evaluator> getEvaluator() {
        if (evaluator == null) {
            evaluator = new ArrayList<Evaluator>();
        }
        return this.evaluator;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the joinBy property.
     * 
     * @return
     *     possible object is
     *     {@link LogicJoinType }
     *     
     */
    public LogicJoinType getJoinBy() {
        return joinBy;
    }

    /**
     * Sets the value of the joinBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link LogicJoinType }
     *     
     */
    public void setJoinBy(LogicJoinType value) {
        this.joinBy = value;
    }

}
