package ru.citeck.ecos.menu.dto;

import java.util.List;

public class ResolvedMenuConfig {

    private String id;
    private String type;
    private List<Item> items;

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public List<Item> getItems() {
        return items;
    }
}
