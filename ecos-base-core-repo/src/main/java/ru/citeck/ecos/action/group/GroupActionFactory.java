package ru.citeck.ecos.action.group;

/**
 * @author Pavel Simonov
 */
public interface GroupActionFactory {

    String[] EMPTY_STR_ARR = new String[0];

    GroupAction createAction(GroupActionConfig config);

    String getActionId();

    default String[] getMandatoryParams() {
        return EMPTY_STR_ARR;
    }
}
