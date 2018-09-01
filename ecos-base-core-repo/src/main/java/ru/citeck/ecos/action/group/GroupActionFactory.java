package ru.citeck.ecos.action.group;

/**
 * @author Pavel Simonov
 */
public interface GroupActionFactory<T> {

    String[] EMPTY_STR_ARR = new String[0];

    GroupAction<T> createAction(GroupActionConfig config);

    String getActionId();

    default String[] getMandatoryParams() {
        return EMPTY_STR_ARR;
    }

    Class<T> getActionType();
}
