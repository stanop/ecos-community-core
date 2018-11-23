package ru.citeck.ecos.menu.dto;

import java.util.List;
import java.util.Map;

public class Element {

    private String id;
    private String type;
    private String label;
    private Icon icon;
    private Boolean mobileVisible;
    private Action action;
    private List<Element> items;
    private Map<String, String> params;

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIcon(String type, String value) {
        Icon i = new Icon();
        i.setType(type);
        i.setValue(value);
        this.icon = i;
    }

    public void setMobileVisible(Boolean mobileVisible) {
        this.mobileVisible = mobileVisible;
    }

    public void setAction(String type, Map<String, String> params) {
        Action action = new Action();
        action.setType(type);
        action.setParams(params);
        this.action = action;
    }

    public void setItems(List<Element> items) {
        this.items = items;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public Icon getIcon() {
        return icon;
    }

    public Boolean getMobileVisible() {
        return mobileVisible;
    }

    public Action getAction() {
        return action;
    }

    public List<Element> getItems() {
        return items;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public static class Action {
        String type;
        Map<String, String> params;

        public void setType(String type) {
            this.type = type;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public String getType() {
            return type;
        }

        public Map<String, String> getParams() {
            return params;
        }
    }

    public static class Icon {
        String type;
        String value;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
