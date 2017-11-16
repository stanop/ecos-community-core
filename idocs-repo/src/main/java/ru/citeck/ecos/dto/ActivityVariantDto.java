package ru.citeck.ecos.dto;

import java.util.List;
import java.util.Map;

/**
 * Activity variant data transfer object
 */
public class ActivityVariantDto {

    /**
     * Type
     */
    private String type;

    /**
     * Title
     */
    private String title;

    /**
     * Form id
     */
    private String formId;

    /**
     * Parent types
     */
    private List<String> parentTypes;

    /**
     * View params
     */
    private Map<String, String> viewParams;

    /** Getters and setters */

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public List<String> getParentTypes() {
        return parentTypes;
    }

    public void setParentTypes(List<String> parentTypes) {
        this.parentTypes = parentTypes;
    }

    public Map<String, String> getViewParams() {
        return viewParams;
    }

    public void setViewParams(Map<String, String> viewParams) {
        this.viewParams = viewParams;
    }
}
