package ru.citeck.ecos.icase.activity.dto;

public enum CaseServiceType {

    ALFRESCO("alf"),
    EPROC("eproc");

    private String shortName;

    CaseServiceType(String shortName) {
        this.shortName = shortName;
    }

    public static CaseServiceType getByShortName(String shortName) {
        for (CaseServiceType caseServiceType : values()) {
            if (caseServiceType.shortName.equals(shortName)) {
                return caseServiceType;
            }
        }
        throw new IllegalArgumentException("CaseServiceType not found for name " + shortName);
    }

    public static CaseServiceType getByShortNameQuietly(String shortName) {
        for (CaseServiceType caseServiceType : values()) {
            if (caseServiceType.shortName.equals(shortName)) {
                return caseServiceType;
            }
        }
        return null;
    }

    public String getShortName() {
        return shortName;
    }
}
