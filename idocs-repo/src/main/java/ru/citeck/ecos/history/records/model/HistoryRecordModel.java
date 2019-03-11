package ru.citeck.ecos.history.records.model;

import ru.citeck.ecos.records2.graphql.meta.annotation.MetaAtt;

import java.util.Date;
import java.util.List;

public class HistoryRecordModel {

    private String id;

    @MetaAtt("event:initiator")
    private EventUserModel initiator;
    @MetaAtt("event:documentId")
    private String documentId;
    @MetaAtt("event:date")
    private Date date;
    @MetaAtt("event:documentVersion")
    private String documentVersion;
    @MetaAtt("event:taskComment")
    private String taskComment;
    @MetaAtt("event:name")
    private String eventType;
    @MetaAtt("event:taskRole")
    private String taskRole;
    @MetaAtt("event:taskOutcome")
    private String taskOutcome;
    @MetaAtt("event:taskOutcomeTitle")
    private String taskOutcomeTitle;
    @MetaAtt("event:taskType")
    private TaskType taskType;
    @MetaAtt("event:taskAttachments")
    private List<TaskAttachmentModel> taskAttachments;
    @MetaAtt("event:taskInstanceId")
    private String taskInstanceId;
    @MetaAtt("event:taskPooledActors")
    private List<EventUserOrGroupModel> taskPooledActors;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventUserModel getInitiator() {
        return initiator;
    }

    public void setInitiator(EventUserModel initiator) {
        this.initiator = initiator;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(String documentVersion) {
        this.documentVersion = documentVersion;
    }

    public String getTaskComment() {
        return taskComment;
    }

    public void setTaskComment(String taskComment) {
        this.taskComment = taskComment;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTaskRole() {
        return taskRole;
    }

    public void setTaskRole(String taskRole) {
        this.taskRole = taskRole;
    }

    public String getTaskOutcome() {
        return taskOutcome;
    }

    public void setTaskOutcome(String taskOutcome) {
        this.taskOutcome = taskOutcome;
    }

    public String getTaskOutcomeTitle() {
        return taskOutcomeTitle;
    }

    public void setTaskOutcomeTitle(String taskOutcomeTitle) {
        this.taskOutcomeTitle = taskOutcomeTitle;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public List<TaskAttachmentModel> getTaskAttachments() {
        return taskAttachments;
    }

    public void setTaskAttachments(List<TaskAttachmentModel> taskAttachments) {
        this.taskAttachments = taskAttachments;
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(String taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public List<EventUserOrGroupModel> getTaskPooledActors() {
        return taskPooledActors;
    }

    public void setTaskPooledActors(List<EventUserOrGroupModel> taskPooledActors) {
        this.taskPooledActors = taskPooledActors;
    }
}
