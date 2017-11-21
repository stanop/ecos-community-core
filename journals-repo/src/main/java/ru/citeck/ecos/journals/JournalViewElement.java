package ru.citeck.ecos.journals;

import ru.citeck.ecos.journals.xml.Option;
import ru.citeck.ecos.journals.xml.ViewElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalViewElement {

    private String template;
    private Map<String, String> params = new HashMap<>();

    public JournalViewElement(ViewElement element) {
        set(element);
    }

    public void set(ViewElement element) {
        if (element != null) {
            if (element.getTemplate() != null) {
                template = element.getTemplate();
            }
            List<Option> xmlParams = element.getParam();
            if (xmlParams != null) {
                for (Option o : xmlParams) {
                    params.put(o.getName(), o.getValue());
                }
            }
        }
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
