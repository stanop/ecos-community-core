
package ru.citeck.ecos.journals.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="imports" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="import" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="uri" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="prefix" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="journal" type="{http://www.citeck.ru/ecos/journals/1.0}journal" maxOccurs="unbounded"/>
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
    "imports",
    "journal"
})
@XmlRootElement(name = "journals")
public class Journals {

    protected Imports imports;
    @XmlElement(required = true)
    protected List<Journal> journal;

    /**
     * Gets the value of the imports property.
     * 
     * @return
     *     possible object is
     *     {@link Imports }
     *     
     */
    public Imports getImports() {
        return imports;
    }

    /**
     * Sets the value of the imports property.
     * 
     * @param value
     *     allowed object is
     *     {@link Imports }
     *     
     */
    public void setImports(Imports value) {
        this.imports = value;
    }

    /**
     * Gets the value of the journal property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the journal property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJournal().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Journal }
     * 
     * 
     */
    public List<Journal> getJournal() {
        if (journal == null) {
            journal = new ArrayList<Journal>();
        }
        return this.journal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Journals journals = (Journals) o;

        if (imports != null ? !imports.equals(journals.imports) : journals.imports != null) return false;
        return journal != null ? journal.equals(journals.journal) : journals.journal == null;

    }

    @Override
    public int hashCode() {
        int result = imports != null ? imports.hashCode() : 0;
        result = 31 * result + (journal != null ? journal.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Journals{" +
                "imports=" + imports +
                ", journal=" + journal +
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
     *         &lt;element name="import" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="uri" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="prefix" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "_import"
    })
    public static class Imports {

        @XmlElement(name = "import", required = true)
        protected List<Import> _import;

        /**
         * Gets the value of the import property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the import property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getImport().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Import }
         * 
         * 
         */
        public List<Import> getImport() {
            if (_import == null) {
                _import = new ArrayList<Import>();
            }
            return this._import;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Imports imports = (Imports) o;

            return _import != null ? _import.equals(imports._import) : imports._import == null;

        }

        @Override
        public int hashCode() {
            return _import != null ? _import.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Imports{" +
                    "_import=" + _import +
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
         *       &lt;attribute name="uri" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="prefix" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Import {

            @XmlAttribute(name = "uri", required = true)
            protected String uri;
            @XmlAttribute(name = "prefix", required = true)
            protected String prefix;

            /**
             * Gets the value of the uri property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUri() {
                return uri;
            }

            /**
             * Sets the value of the uri property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUri(String value) {
                this.uri = value;
            }

            /**
             * Gets the value of the prefix property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPrefix() {
                return prefix;
            }

            /**
             * Sets the value of the prefix property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPrefix(String value) {
                this.prefix = value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Import anImport = (Import) o;

                if (uri != null ? !uri.equals(anImport.uri) : anImport.uri != null) return false;
                return prefix != null ? prefix.equals(anImport.prefix) : anImport.prefix == null;

            }

            @Override
            public int hashCode() {
                int result = uri != null ? uri.hashCode() : 0;
                result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
                return result;
            }

            @Override
            public String toString() {
                return "Import{" +
                        "uri='" + uri + '\'' +
                        ", prefix='" + prefix + '\'' +
                        '}';
            }
        }

    }

}
