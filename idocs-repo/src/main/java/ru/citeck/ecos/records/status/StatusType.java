package ru.citeck.ecos.records.status;

/**
 * @author Roman Makarskiy
 */
public enum StatusType {

    CASE_STATUS("case-status"),
    DOCUMENT_STATUS("document-status");

    private String name;

    StatusType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
