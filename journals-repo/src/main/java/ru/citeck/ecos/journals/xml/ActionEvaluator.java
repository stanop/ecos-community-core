package ru.citeck.ecos.journals.xml;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "actionEvaluator", propOrder = {
        "param"
})
public class ActionEvaluator {

    @XmlAttribute(name = "id")
    protected String id;
    protected List<Option> param;

    public List<Option> getParam() {
        if (param == null) {
            param = new ArrayList<>();
        }
        return this.param;
    }
}
