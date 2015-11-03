
package ru.citeck.ecos.invariants.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for language.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="language">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="javascript"/>
 *     &lt;enumeration value="freemarker"/>
 *     &lt;enumeration value="criteria"/>
 *     &lt;enumeration value="lucene"/>
 *     &lt;enumeration value="explicit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "language")
@XmlEnum
public enum Language {

    @XmlEnumValue("javascript")
    JAVASCRIPT("javascript"),
    @XmlEnumValue("freemarker")
    FREEMARKER("freemarker"),
    @XmlEnumValue("criteria")
    CRITERIA("criteria"),
    @XmlEnumValue("lucene")
    LUCENE("lucene"),
    @XmlEnumValue("explicit")
    EXPLICIT("explicit");
    private final String value;

    Language(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Language fromValue(String v) {
        for (Language c: Language.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
