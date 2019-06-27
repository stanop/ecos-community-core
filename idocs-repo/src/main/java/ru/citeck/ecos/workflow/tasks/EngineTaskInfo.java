package ru.citeck.ecos.workflow.tasks;

import ru.citeck.ecos.records2.RecordRef;

import java.util.Map;

public class EngineTaskInfo implements TaskInfo {

    private final String engineId;
    private final TaskInfo info;

    EngineTaskInfo(String engineId, TaskInfo localInfo) {
        this.engineId = engineId;
        this.info = localInfo;
    }

    @Override
    public String getTitle() {
        return info.getTitle();
    }

    @Override
    public String getId() {
        return engineId + "$" + info.getId();
    }

    @Override
    public String getAssignee() {
        return info.getAssignee();
    }

    @Override
    public String getCandidate() {
        return info.getCandidate();
    }

    @Override
    public String getFormKey() {
        return info.getFormKey();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return info.getAttributes();
    }

    @Override
    public Map<String, Object> getLocalAttributes() {
        return info.getLocalAttributes();
    }

    @Override
    public RecordRef getDocument() {
        return info.getDocument();
    }

    @Override
    public Object getAttribute(String name) {
        return info.getAttribute(name);
    }
}
