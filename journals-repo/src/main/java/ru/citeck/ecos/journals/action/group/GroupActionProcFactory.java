package ru.citeck.ecos.journals.action.group;

import java.util.Map;

/**
 * @author Pavel Simonov
 */
public interface GroupActionProcFactory {

    String[] EMPTY_STR_ARR = new String[0];

    GroupActionProcessor createProcessor(Map<String, String> params);

    String getActionId();

    default String[] getMandatoryParams() {
        return EMPTY_STR_ARR;
    }
}
