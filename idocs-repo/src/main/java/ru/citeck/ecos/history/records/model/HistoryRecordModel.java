package ru.citeck.ecos.history.records.model;

import ru.citeck.ecos.graphql.meta.annotation.MetaAtt;

import java.util.Date;
import java.util.List;

public class HistoryRecordModel {

    public String nodeRef;

    @MetaAtt("event:initiator")
    public EventUserModel initiator;
    @MetaAtt("event:documentId")
    public String documentId;
    @MetaAtt("event:date")
    public Date date;
    @MetaAtt("event:documentVersion")
    public String documentVersion;
    @MetaAtt("event:taskComment")
    public String taskComment;
    @MetaAtt("event:name")
    public String eventType;
    @MetaAtt("event:taskRole")
    public String taskRole;
    @MetaAtt("event:taskOutcome")
    public String taskOutcome;
    @MetaAtt("event:taskOutcomeTitle")
    public String taskOutcomeTitle;
    @MetaAtt("event:taskType")
    public TaskType taskType;
    @MetaAtt("event:taskAttachments")
    public List<TaskAttachmentModel> taskAttachments;
    @MetaAtt("event:taskInstanceId")
    public String taskInstanceId;
    @MetaAtt("event:taskPooledActors")
    public List<EventUserOrGroupModel> taskPooledActors;

    public String getNodeRef() {
        return nodeRef;
    }

    public EventUserModel getInitiator() {
        return initiator;
    }

    public String getDocumentId() {
        return documentId;
    }

    public Date getDate() {
        return date;
    }

    public String getDocumentVersion() {
        return documentVersion;
    }

    public String getTaskComment() {
        return taskComment;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTaskRole() {
        return taskRole;
    }

    public String getTaskOutcome() {
        return taskOutcome;
    }

    public String getTaskOutcomeTitle() {
        return taskOutcomeTitle;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public List<TaskAttachmentModel> getTaskAttachments() {
        return taskAttachments;
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public List<EventUserOrGroupModel> getTaskPooledActors() {
        return taskPooledActors;
    }
}
