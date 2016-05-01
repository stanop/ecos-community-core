package ru.citeck.ecos.action;

import java.util.HashMap;
import java.util.Map;

/**
 * @author deathNC on 30.04.2016.
 */
public class NodeActionDefinition {

    private String title;

    private String url;

    private Map<String, String> properties = new HashMap<>();

    public NodeActionDefinition() {
    }

    public NodeActionDefinition(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public Map<String, String> getFieldsMap() {
        Map<String, String> fieldsMap = new HashMap<>();
        fieldsMap.put("title", title);
        fieldsMap.put("url", url);
        fieldsMap.putAll(properties);
        return fieldsMap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
