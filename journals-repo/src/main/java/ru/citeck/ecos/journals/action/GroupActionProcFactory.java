package ru.citeck.ecos.journals.action;

/**
 * @author Pavel Simonov
 */
public interface GroupActionProcFactory {

    GroupActionProcessor getProcessor();

    String getActionId();
}
