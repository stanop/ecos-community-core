package ru.citeck.ecos.action.node;

import java.util.*;

/**
 * @author deathNC on 30.04.2016.
 * @author Pavel Simonov
 */
public abstract class NodeActionDefinition {

    public static final String PROP_ACTION_TYPE = "actionType";
    public static final String PROP_TITLE = "title";
    public static final String PROP_ACTION_ID = "actionId";

    private Set<String> equalityIgnoredProperties = new HashSet<>();
    private Map<String, String> properties = new HashMap<>();

    public NodeActionDefinition() {
        properties.put(PROP_ACTION_TYPE, getActionType());
        ignorePropertyEquality(PROP_TITLE);
        ignorePropertyEquality(PROP_ACTION_ID);
    }

    public void ignorePropertyEquality(String key) {
        equalityIgnoredProperties.add(key);
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public String getTitle() {
        return getProperty(PROP_TITLE);
    }

    public void setTitle(String title) {
        setProperty(PROP_TITLE, title);
    }

    public String getActionId() {
        return getProperty(PROP_ACTION_ID);
    }

    public void setActionId(String actionId) {
        setProperty(PROP_ACTION_ID, actionId);
    }

    protected abstract String getActionType();

    public boolean isValid() {
        for (String value : properties.values()) {
            if (value == null) return false;
        }
        return true;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeActionDefinition that = (NodeActionDefinition) o;

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!equalityIgnoredProperties.contains(entry.getKey())
                    && !Objects.equals(entry.getValue(), that.properties.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (!equalityIgnoredProperties.contains(entry.getKey())) {
                h += entry.getValue().hashCode();
            }
        }
        return h;
    }
}
