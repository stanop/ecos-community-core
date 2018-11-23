package ru.citeck.ecos.history.records.model;

import ru.citeck.ecos.graphql.meta.annotation.MetaAtt;

import java.util.Date;
import java.util.List;

public class HistoryRecordModel {

    public String nodeRef;

    @MetaAtt(name = "event:initiator")
    public EventUserModel initiator;
    @MetaAtt(name = "event:documentId")
    public String documentId;
    @MetaAtt(name = "event:date")
    public Date date;
    @MetaAtt(name = "event:documentVersion")
    public String documentVersion;
    @MetaAtt(name = "event:taskComment")
    public String taskComment;
    @MetaAtt(name = "event:name")
    public String eventType;
    @MetaAtt(name = "event:taskRole")
    public String taskRole;
    @MetaAtt(name = "event:taskOutcome")
    public String taskOutcome;
    @MetaAtt(name = "event:taskOutcomeTitle")
    public String taskOutcomeTitle;
    @MetaAtt(name = "event:taskType")
    public TaskType taskType;
    @MetaAtt(name = "event:taskAttachments")
    public List<TaskAttachmentModel> taskAttachments;
    @MetaAtt(name = "event:taskInstanceId")
    public String taskInstanceId;
    @MetaAtt(name = "event:taskPooledActors")
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
