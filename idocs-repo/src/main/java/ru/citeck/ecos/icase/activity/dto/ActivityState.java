package ru.citeck.ecos.icase.activity.dto;

import org.apache.commons.lang.StringUtils;

public enum ActivityState {

    NOT_STARTED("Not started"),
    STARTED("Started"),
    COMPLETED("Completed");

    private String value;

    ActivityState(String value) {
        this.value = value;
    }

    public static ActivityState getByValue(String value) {
        for (ActivityState state : ActivityState.values()) {
            if (StringUtils.equalsIgnoreCase(state.value, value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("State with value " + value + " not found in enum");
    }

    public String getValue() {
        return value;
    }

}
