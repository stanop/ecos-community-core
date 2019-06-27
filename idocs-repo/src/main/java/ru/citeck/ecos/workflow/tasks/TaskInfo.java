package ru.citeck.ecos.workflow.tasks;

import ru.citeck.ecos.records2.RecordRef;

import java.util.Map;

public interface TaskInfo {

    String getId();

    String getTitle();

    String getAssignee();

    String getCandidate();

    String getFormKey();

    Map<String, Object> getAttributes();

    Map<String, Object> getLocalAttributes();

    RecordRef getDocument();

    Object getAttribute(String name);
}
