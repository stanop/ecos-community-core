package ru.citeck.ecos.icase.activity.create;

import org.alfresco.service.namespace.QName;

import java.util.*;

public class ActivityCreateVariant {

    private String id;
    private QName type;
    private String title;
    private String formId;
    private Map<String, String> viewParams = new HashMap<>();
    private List<ActivityCreateVariant> children = new ArrayList<>();
    private boolean canBeCreated = false;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public QName getType() {
        return type;
    }

    public String getPrefixedType() {
        return type.toPrefixString();
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getFormId() {
        return formId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void addChild(ActivityCreateVariant cv) {
        children.add(cv);
    }

    public List<ActivityCreateVariant> getChildren() {
        return children;
    }

    public void setViewParam(String key, String value) {
        this.viewParams.put(key, value);
    }

    public Map<String, String> getViewParams() {
        return viewParams;
    }

    public boolean getCanBeCreated() {
        return canBeCreated;
    }

    public void setCanBeCreated(boolean canBeCreated) {
        this.canBeCreated = canBeCreated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityCreateVariant that = (ActivityCreateVariant) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
