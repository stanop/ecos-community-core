package ru.citeck.ecos.menu.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

public class Element {

    private String id;
    private String label;
    private String icon;
    private Boolean mobileVisible;
    private Action action;
    private List<Element> items;
    @JsonIgnore
    private String contextId;

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
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

    public String getContextId() {
        return contextId;
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

}
