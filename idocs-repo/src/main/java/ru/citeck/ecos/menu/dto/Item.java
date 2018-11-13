package ru.citeck.ecos.menu.dto;

import java.util.List;
import java.util.Map;

public class Item {

    private String id;
    private String label;
    private String icon;
    private Boolean mobileVisible;
    private List<Item> items;
    private Action action;

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

    public void setItems(List<Item> items) {
        this.items = items;
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

    public List<Item> getItems() {
        return items;
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
