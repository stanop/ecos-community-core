
package ru.citeck.ecos.journals.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for logicJoinType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="logicJoinType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OR"/>
 *     &lt;enumeration value="AND"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "logicJoinType")
@XmlEnum
public enum LogicJoinType {

    OR,
    AND;

    public String value() {
        return name();
    }

    public static LogicJoinType fromValue(String v) {
        return valueOf(v);
    }

}
