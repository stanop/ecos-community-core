/**
 * CMMN prefixes definitions
 *
 * @author deathNC
 */
@javax.xml.bind.annotation.XmlSchema(
        xmlns = {
                @javax.xml.bind.annotation.XmlNs(prefix = "xsd", namespaceURI = "http://www.w3.org/2001/XMLSchema"),
                @javax.xml.bind.annotation.XmlNs(prefix = "dc", namespaceURI = "http://www.omg.org/spec/CMMN/20151109/DC"),
                @javax.xml.bind.annotation.XmlNs(prefix = "di", namespaceURI = "http://www.omg.org/spec/CMMN/20151109/DI"),
                @javax.xml.bind.annotation.XmlNs(prefix = "cmmndi", namespaceURI = "http://www.omg.org/spec/CMMN/20151109/CMMNDI"),
                @javax.xml.bind.annotation.XmlNs(prefix = "cmmn", namespaceURI = "http://www.omg.org/spec/CMMN/20151109/MODEL"),
                @javax.xml.bind.annotation.XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance")
        }
) package ru.citeck.ecos.cmmn.model;