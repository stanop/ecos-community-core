package ru.citeck.ecos.cmmn.condition;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Strizhov
 */
@XmlRootElement(name = "condition")
@XmlAccessorType(XmlAccessType.FIELD)
public class Condition {
    private QName type;
    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<ConditionProperty> properties = new ArrayList<>();

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public List<ConditionProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<ConditionProperty> properties) {
        this.properties = properties;
    }
}
