package ru.citeck.ecos.action;

import java.util.HashMap;
import java.util.Map;

/**
 * @author deathNC on 30.04.2016.
 */
public class NodeActionDefinition {

    public static final String NODE_ACTION_TYPE_SERVER_ACTION = "serverAction";
    public static final String NODE_ACTION_TYPE_REDIRECT = "redirect";

    private String title;

    private String url;

    private String actionType = NODE_ACTION_TYPE_SERVER_ACTION;

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
        fieldsMap.put("actionType", actionType);
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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
