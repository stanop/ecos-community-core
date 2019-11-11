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
@XmlType(name = "action", propOrder = {
        "param",
        "evaluator"
})
public class Action {

    protected List<Option> param;
    protected ActionEvaluator evaluator;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "title", required = true)
    protected String title;
    @XmlAttribute(name = "type")
    protected String type;

    public List<Option> getParam() {
        if (param == null) {
            param = new ArrayList<>();
        }
        return this.param;
    }

}
