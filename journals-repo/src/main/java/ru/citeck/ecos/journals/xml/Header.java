
package ru.citeck.ecos.journals.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for header complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="header">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="option" type="{http://www.citeck.ru/ecos/journals/1.0}option" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="batch-edit" type="{http://www.citeck.ru/ecos/journals/1.0}batchEdit" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="filter" type="{http://www.citeck.ru/ecos/journals/1.0}filter" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="key" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="default" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="visible" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="searchable" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="sortable" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="groupable" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "header", propOrder = {
    "option",
    "batchEdit",
    "filter"
})
public class Header {

    protected List<Option> option;
    @XmlElement(name = "batch-edit")
    protected List<BatchEdit> batchEdit;
    protected Filter filter;
    @XmlAttribute(name = "key", required = true)
    protected String key;
    @XmlAttribute(name = "default")
    protected Boolean _default;
    @XmlAttribute(name = "visible")
    protected Boolean visible;
    @XmlAttribute(name = "searchable")
    protected Boolean searchable;
    @XmlAttribute(name = "sortable")
    protected Boolean sortable;
    @XmlAttribute(name = "groupable")
    protected Boolean groupable;

    /**
     * Gets the value of the option property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the option property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOption().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Option }
     * 
     * 
     */
    public List<Option> getOption() {
        if (option == null) {
            option = new ArrayList<Option>();
        }
        return this.option;
    }

    /**
     * Gets the value of the batchEdit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the batchEdit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBatchEdit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BatchEdit }
     * 
     * 
     */
    public List<BatchEdit> getBatchEdit() {
        if (batchEdit == null) {
            batchEdit = new ArrayList<BatchEdit>();
        }
        return this.batchEdit;
    }

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link Filter }
     *     
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Filter }
     *     
     */
    public void setFilter(Filter value) {
        this.filter = value;
    }

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDefault() {
        if (_default == null) {
            return false;
        } else {
            return _default;
        }
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefault(Boolean value) {
        this._default = value;
    }

    /**
     * Gets the value of the visible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVisible() {
        if (visible == null) {
            return true;
        } else {
            return visible;
        }
    }

    /**
     * Sets the value of the visible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVisible(Boolean value) {
        this.visible = value;
    }

    /**
     * Gets the value of the searchable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSearchable() {
        if (searchable == null) {
            return true;
        } else {
            return searchable;
        }
    }

    /**
     * Sets the value of the searchable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSearchable(Boolean value) {
        this.searchable = value;
    }

    /**
     * Gets the value of the sortable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSortable() {
        if (sortable == null) {
            return true;
        } else {
            return sortable;
        }
    }

    /**
     * Sets the value of the sortable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSortable(Boolean value) {
        this.sortable = value;
    }

    /**
     * Gets the value of the groupable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGroupable() {
        if (groupable == null) {
            return false;
        } else {
            return groupable;
        }
    }

    /**
     * Sets the value of the groupable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGroupable(Boolean value) {
        this.groupable = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Header header = (Header) o;

        if (option != null ? !option.equals(header.option) : header.option != null) return false;
        if (key != null ? !key.equals(header.key) : header.key != null) return false;
        if (_default != null ? !_default.equals(header._default) : header._default != null) return false;
        if (visible != null ? !visible.equals(header.visible) : header.visible != null) return false;
        if (searchable != null ? !searchable.equals(header.searchable) : header.searchable != null) return false;
        if (sortable != null ? !sortable.equals(header.sortable) : header.sortable != null) return false;
        return groupable != null ? groupable.equals(header.groupable) : header.groupable == null;

    }

    @Override
    public int hashCode() {
        int result = option != null ? option.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (_default != null ? _default.hashCode() : 0);
        result = 31 * result + (visible != null ? visible.hashCode() : 0);
        result = 31 * result + (searchable != null ? searchable.hashCode() : 0);
        result = 31 * result + (sortable != null ? sortable.hashCode() : 0);
        result = 31 * result + (groupable != null ? groupable.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Header{" +
                "option=" + option +
                ", key='" + key + '\'' +
                ", _default=" + _default +
                ", visible=" + visible +
                ", searchable=" + searchable +
                ", sortable=" + sortable +
                ", groupable=" + groupable +
                '}';
    }
}
