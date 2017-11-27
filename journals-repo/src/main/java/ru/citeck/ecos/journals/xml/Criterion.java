
package ru.citeck.ecos.journals.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import ru.citeck.ecos.invariants.xml.Invariant;


/**
 * <p>Java class for criterion complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="criterion">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.citeck.ru/ecos/journals/1.0}viewElement">
 *       &lt;sequence>
 *         &lt;element name="region" type="{http://www.citeck.ru/ecos/journals/1.0}criterionRegion" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="invariant" type="{http://www.citeck.ru/ecos/invariants/1.0}invariant" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "criterion", propOrder = {
    "region",
    "invariant"
})
public class Criterion
    extends ViewElement
{

    protected List<CriterionRegion> region;
    protected List<Invariant> invariant;

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
     * {@link CriterionRegion }
     * 
     * 
     */
    public List<CriterionRegion> getRegion() {
        if (region == null) {
            region = new ArrayList<CriterionRegion>();
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

}
