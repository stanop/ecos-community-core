package ru.citeck.ecos.history.records.model;

public class TaskType {

    private String shortName;

    public TaskType() {
    }

    public TaskType(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
