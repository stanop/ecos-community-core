package ru.citeck.ecos.cmmn.condition;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Strizhov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConditionsList {
    @XmlElementWrapper(name = "conditions")
    @XmlElement(name = "condition")
    private List<Condition> conditions = new ArrayList<>();

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
}
