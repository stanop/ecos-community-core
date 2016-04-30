package ru.citeck.ecos.action;

import java.util.HashMap;
import java.util.Map;

/**
 * @author deathNC on 30.04.2016.
 */
public class NodeActionDefinition {

    private String title;

    private String id;

    private String url;


    public NodeActionDefinition() {
    }

    public NodeActionDefinition(String title, String id, String url) {
        this.title = title;
        this.id = id;
        this.url = url;
    }

    public Map<String, String> getFieldsMap() {
        Map<String, String> fieldsMap = new HashMap<>();
        fieldsMap.put("id", id);
        fieldsMap.put("title", title);
        fieldsMap.put("url", url);
        return fieldsMap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
