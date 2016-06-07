
package ru.citeck.ecos.journals.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for journal complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="journal">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="option" type="{http://www.citeck.ru/ecos/journals/1.0}option" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="headers">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="header" type="{http://www.citeck.ru/ecos/journals/1.0}header" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "journal", propOrder = {
    "option",
    "headers"
})
public class Journal {

    protected List<Option> option;
    @XmlElement(required = true)
    protected Journal.Headers headers;
    @XmlAttribute(name = "id", required = true)
    protected String id;

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
     * Gets the value of the headers property.
     * 
     * @return
     *     possible object is
     *     {@link Journal.Headers }
     *     
     */
    public Journal.Headers getHeaders() {
        return headers;
    }

    /**
     * Sets the value of the headers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Journal.Headers }
     *     
     */
    public void setHeaders(Journal.Headers value) {
        this.headers = value;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Journal journal = (Journal) o;

        if (option != null ? !option.equals(journal.option) : journal.option != null) return false;
        if (headers != null ? !headers.equals(journal.headers) : journal.headers != null) return false;
        return id != null ? id.equals(journal.id) : journal.id == null;

    }

    @Override
    public int hashCode() {
        int result = option != null ? option.hashCode() : 0;
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Journal{" +
                "option=" + option +
                ", headers=" + headers +
                ", id='" + id + '\'' +
                '}';
    }

    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="header" type="{http://www.citeck.ru/ecos/journals/1.0}header" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "header"
    })
    public static class Headers {

        @XmlElement(required = true)
        protected List<Header> header;

        /**
         * Gets the value of the header property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the header property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getHeader().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Header }
         * 
         * 
         */
        public List<Header> getHeader() {
            if (header == null) {
                header = new ArrayList<Header>();
            }
            return this.header;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Headers headers = (Headers) o;

            return header != null ? header.equals(headers.header) : headers.header == null;

        }

        @Override
        public int hashCode() {
            return header != null ? header.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Headers{" +
                    "header=" + header +
                    '}';
        }
    }

}
