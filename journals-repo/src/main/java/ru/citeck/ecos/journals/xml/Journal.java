//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.24 at 01:02:21 PM GMT+07:00
//


package ru.citeck.ecos.journals.xml;

import lombok.Getter;
import lombok.Setter;

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
 *         &lt;element name="datasource" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="predicate" type="{http://www.citeck.ru/ecos/journals/1.0}predicate" minOccurs="0"/>
 *         &lt;element name="create" type="{http://www.citeck.ru/ecos/journals/1.0}createVariants" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="group-by" type="{http://www.citeck.ru/ecos/journals/1.0}groupBy" minOccurs="0"/>
 *         &lt;element name="option" type="{http://www.citeck.ru/ecos/journals/1.0}option" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="group-actions" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="action" type="{http://www.citeck.ru/ecos/journals/1.0}groupAction" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "journal", propOrder = {
        "datasource",
        "predicate",
        "create",
        "groupBy",
        "option",
        "actions",
        "groupActions",
        "headers"
})
public class Journal {

    protected String datasource;
    protected Predicate predicate;
    protected CreateVariants create;

    @XmlElement(name = "group-by")
    protected GroupBy groupBy;

    protected List<Option> option;

    @Getter
    @Setter
    @XmlElement(name = "actions")
    protected Action actions;

    @XmlElement(name = "group-actions")
    protected GroupActions groupActions;

    @XmlElement(required = true)
    protected Headers headers;

    @XmlAttribute(name = "id", required = true)
    protected String id;

    /**
     * Gets the value of the datasource property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDatasource() {
        return datasource;
    }

    /**
     * Sets the value of the datasource property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDatasource(String value) {
        this.datasource = value;
    }

    /**
     * Gets the value of the predicate property.
     *
     * @return possible object is
     * {@link Predicate }
     */
    public Predicate getPredicate() {
        return predicate;
    }

    /**
     * Sets the value of the predicate property.
     *
     * @param value allowed object is
     *              {@link Predicate }
     */
    public void setPredicate(Predicate value) {
        this.predicate = value;
    }

    /**
     * Gets the value of the create property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the create property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCreate().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CreateVariants }
     */
    public CreateVariants getCreate() {
        return this.create;
    }

    /**
     * Gets the value of the groupBy property.
     *
     * @return possible object is
     * {@link GroupBy }
     */
    public GroupBy getGroupBy() {
        return groupBy;
    }

    /**
     * Sets the value of the groupBy property.
     *
     * @param value allowed object is
     *              {@link GroupBy }
     */
    public void setGroupBy(GroupBy value) {
        this.groupBy = value;
    }

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
     */
    public List<Option> getOption() {
        if (option == null) {
            option = new ArrayList<>();
        }
        return this.option;
    }

    /**
     * Gets the value of the groupActions property.
     *
     * @return possible object is
     * {@link GroupActions }
     */
    public GroupActions getGroupActions() {
        return groupActions;
    }

    /**
     * Sets the value of the groupActions property.
     *
     * @param value allowed object is
     *              {@link GroupActions }
     */
    public void setGroupActions(GroupActions value) {
        this.groupActions = value;
    }

    /**
     * Gets the value of the headers property.
     *
     * @return possible object is
     * {@link Headers }
     */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * Sets the value of the headers property.
     *
     * @param value allowed object is
     *              {@link Headers }
     */
    public void setHeaders(Headers value) {
        this.headers = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) {
        this.id = value;
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
     *         &lt;element name="action" type="{http://www.citeck.ru/ecos/journals/1.0}groupAction" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "action"
    })
    public static class GroupActions {

        @XmlElement(required = true)
        protected List<GroupAction> action;

        /**
         * Gets the value of the action property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the action property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAction().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GroupAction }
         */
        public List<GroupAction> getAction() {
            if (action == null) {
                action = new ArrayList<>();
            }
            return this.action;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "action"
    })
    public static class Action {

        @XmlElement(required = true)
        protected List<ru.citeck.ecos.journals.xml.Action> action;

        public List<ru.citeck.ecos.journals.xml.Action> getAction() {
            if (action == null) {
                action = new ArrayList<>();
            }
            return this.action;
        }

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
         */
        public List<Header> getHeader() {
            if (header == null) {
                header = new ArrayList<>();
            }
            return this.header;
        }

    }

}
