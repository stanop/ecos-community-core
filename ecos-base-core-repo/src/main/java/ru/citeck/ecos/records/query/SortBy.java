package ru.citeck.ecos.records.query;

public class SortBy {

    private String attribute;
    private boolean ascending;

    public SortBy() {
    }

    public SortBy(String attribute, boolean ascending) {
        this.attribute = attribute;
        this.ascending = ascending;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public boolean isAscending() {
        return ascending;
    }
}
