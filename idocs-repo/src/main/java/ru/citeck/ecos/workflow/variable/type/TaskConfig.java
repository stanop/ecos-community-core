package ru.citeck.ecos.workflow.variable.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TaskConfig implements EcosPojoType {

    private static final String GROUP_PREFIX = "GROUP_";

    private List<String> candidateUsers;
    private List<String> candidateGroups;
    private Date dueDate;
    private String formKey;
    private String category;
    private String assignee;
    private int priority = 0;

    public void addCandidate(String authorityName) {
        if (StringUtils.isNotBlank(authorityName)) {
            if (authorityName.startsWith(GROUP_PREFIX)) {
                addCandidateGroup(authorityName);
            } else {
                addCandidateUser(authorityName);
            }
        }
    }

    @JsonIgnore
    public void setPerformer(String authorityName) {
        if (StringUtils.isNotBlank(authorityName)) {
            if (authorityName.startsWith(GROUP_PREFIX)) {
                addCandidateGroup(authorityName);
            } else {
                assignee = authorityName;
            }
        }
    }

    @JsonIgnore
    public String getPerformer() {
        String result = null;
        if (StringUtils.isNotEmpty(assignee)) {
            result = assignee;
        } else if ((candidateUsers == null || candidateUsers.isEmpty())
                    && candidateGroups != null && candidateGroups.size() == 1) {
            result = candidateGroups.get(0);
        }
        return result;
    }

    private void addCandidateUser(String userName) {
        if (candidateUsers == null) {
            candidateUsers = new ArrayList<>();
        }
        candidateUsers.add(userName);
    }

    private void addCandidateGroup(String authorityName) {
        if (candidateGroups == null) {
            candidateGroups = new ArrayList<>();
        }
        candidateGroups.add(authorityName);
    }

    @JsonProperty(value = "u")
    public List<String> getCandidateUsers() {
        return candidateUsers != null ? candidateUsers : Collections.emptyList();
    }

    @JsonProperty(value = "u")
    public void setCandidateUsers(List<String> candidateUsers) {
        this.candidateUsers = candidateUsers;
    }

    @JsonProperty(value = "g")
    public List<String> getCandidateGroups() {
        return candidateGroups != null ? candidateGroups : Collections.emptyList();
    }

    @JsonProperty(value = "g")
    public void setCandidateGroups(List<String> candidateGroups) {
        this.candidateGroups = candidateGroups;
    }

    @JsonProperty(value = "d")
    public Date getDueDate() {
        return dueDate;
    }

    @JsonProperty(value = "d")
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @JsonProperty(value = "f")
    public String getFormKey() {
        return formKey;
    }

    @JsonProperty(value = "f")
    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    @JsonProperty(value = "c")
    public String getCategory() {
        return category;
    }

    @JsonProperty(value = "c")
    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty(value = "a")
    public String getAssignee() {
        return assignee;
    }

    @JsonProperty(value = "a")
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    @JsonProperty(value = "p")
    public int getPriority() {
        return priority;
    }

    @JsonProperty(value = "p")
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
