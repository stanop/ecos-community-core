/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
if (Alfresco.component.WorkflowForm) {
    Alfresco.component.WorkflowForm.prototype._loadWorkflowForm= function() {
           /**
        * YUI Library aliases
        */
       var Dom = YAHOO.util.Dom,
          Event = YAHOO.util.Event,
          Selector = YAHOO.util.Selector;

       /**
        * Alfresco Slingshot aliases
        */
       var $html = Alfresco.util.encodeHTML,
          $siteURL = Alfresco.util.siteURL,
          $userProfileLink = Alfresco.util.userProfileLink;

        if (this.isReady && this.workflow)
             {
                // Display the view diagrambutton if diagram is available
                if (this.workflow.diagramUrl)
                {
                   Dom.removeClass(this.id + "-viewWorkflowDiagram");
                   Alfresco.util.createYUIButton(this, "viewWorkflowDiagram", this.viewWorkflowDiagram);
                }

                // Split the task list in current and history tasks and save the most recent one
                var tasks = this.workflow.tasks, recentTask;
                for (var i = 0, il = tasks.length; i < il; i++)
                {
                   if (tasks[i].state == "COMPLETED")
                   {
                      this.historyTasks.push(tasks[i]);
                   }
                   else
                   {
                      this.currentTasks.push(tasks[i]);
                   }
                }

                var sortByDate = function(dateStr1, dateStr2)
                {
                   var date1 = Alfresco.util.fromISO8601(dateStr1),
                      date2 = Alfresco.util.fromISO8601(dateStr2);
                   if (date1 && date2)
                   {
                      return date1 < date2 ? 1 : -1;
                   }
                   else
                   {
                      return !date1 ? 1 : -1;
                   }
                };

                // Sort tasks by completion date
                this.currentTasks.sort(function(task1, task2)
                {
                   return sortByDate(task1.properties.bpm_dueDate, task2.properties.bpm_dueDate);
                });

                // Sort tasks by completion date
                this.historyTasks.sort(function(task1, task2)
                {
                   return sortByDate(task1.properties.bpm_completionDate, task2.properties.bpm_completionDate);
                });
                // Save the most recent task
                var findLastCompleted = function(allHistoryTasks)
                {
                    if (allHistoryTasks.length > 0)
                    {
                        for (var i = 0, il = allHistoryTasks.length; i < il; i++)
                        {
                            var status = allHistoryTasks[i].properties.bpm_status;
                            if (status == "Completed")
                            {
                                return allHistoryTasks[i];
                            }
                        }
                    }
                    return { properties: {} };
                }
                recentTask = findLastCompleted(this.historyTasks);

                // Set values in the "Summary" & "General" form sections
                Dom.get(this.id + "-recentTaskTitle").innerHTML = $html(recentTask.title || "");
                Dom.get(this.id + "-recentTaskTitle").setAttribute("href", this._getTaskUrl("task-details", recentTask.id));

                Dom.get(this.id + "-title").innerHTML = $html(this.workflow.title);
                Dom.get(this.id + "-description").innerHTML = $html(this.workflow.description);
                
                var message = this.workflow.message;
                if (message === null)
                {
                   message = this.msg("workflow.no_message");
                }
                Dom.get(this.id + "-message").innerHTML = $html(message);
                
                var recentTaskOwnersCommentEl=Dom.get(this.id + "-recentTaskOwnersComment")
                recentTaskOwnersCommentEl.innerHTML = recentTask.properties.bpm_comment || this.msg("label.noComment");
                Dom.addClass(recentTaskOwnersCommentEl,"restore-lists");

                var taskOwner = recentTask.owner || {},
                   taskOwnerAvatar = taskOwner.avatar,
                   taskOwnerLink = Alfresco.util.userProfileLink(taskOwner.userName, taskOwner.firstName + " " + taskOwner.lastName, null, !taskOwner.firstName);
                Dom.get(this.id + "-recentTaskOwnersAvatar").setAttribute("src", taskOwnerAvatar ? Alfresco.constants.PROXY_URI + taskOwnerAvatar  + "?c=force" : Alfresco.constants.URL_RESCONTEXT + "components/images/no-user-photo-64.png");
                Dom.get(this.id + "-recentTaskOwnersCommentLink").innerHTML = this.msg("label.recentTaskOwnersCommentLink", taskOwnerLink);

                var initiator = this.workflow.initiator || {};
                Dom.get(this.id + "-startedBy").innerHTML = Alfresco.util.userProfileLink(
                      initiator.userName || this.msg("label.usernameDeleted"), initiator.firstName + " " + initiator.lastName, null, !initiator.firstName);

                var dueDate = Alfresco.util.fromISO8601(this.workflow.dueDate);
                if (dueDate)
                {
                   Dom.get(this.id + "-dueSummary").innerHTML = this.msg("label.dueOn", Alfresco.util.formatDate(dueDate, "defaultDateOnly"));
                   Dom.get(this.id + "-due").innerHTML = Alfresco.util.formatDate(dueDate, "defaultDateOnly");
                }
                else
                {
                   Dom.get(this.id + "-dueSummary").innerHTML = this.msg("label.noDueDate");
                   Dom.get(this.id + "-due").innerHTML = this.msg("label.none");
                }

                var taskCompletionDate = Alfresco.util.fromISO8601(recentTask.properties.bpm_completionDate);
                Dom.get(this.id + "-recentTaskCompletedOn").innerHTML = $html(taskCompletionDate ? Alfresco.util.formatDate(taskCompletionDate, "mediumDate") : this.msg("label.notCompleted"));

                Dom.get(this.id + "-recentTaskCompletedBy").innerHTML = taskOwner.userName ? taskOwnerLink : this.msg("label.notCompleted");

                Dom.get(this.id + "-recentTaskOutcome").innerHTML = $html(recentTask.outcome || "");

                var workflowCompletedDate = Alfresco.util.fromISO8601(this.workflow.endDate);
                Dom.get(this.id + "-completed").innerHTML = $html(workflowCompletedDate ? Alfresco.util.formatDate(workflowCompletedDate) : this.msg("label.notCompleted"));

                var startDate = Alfresco.util.fromISO8601(this.workflow.startDate);
                if (startDate)
                {
                   Dom.get(this.id + "-started").innerHTML = Alfresco.util.formatDate(startDate);
                }

                var priorityMap = { "1": "high", "2": "medium", "3": "low" },
                   priorityKey = priorityMap[this.workflow.priority + ""],
                   priority = this.msg("priority." + priorityKey),
                   priorityLabel = this.msg("label.priorityLevel", priority);
                var prioritySummaryEl = Dom.get(this.id + "-prioritySummary");
                Dom.addClass(prioritySummaryEl, priorityKey);
                prioritySummaryEl.innerHTML = priorityLabel;
                Dom.get(this.id + "-priority").innerHTML = priority;

                var status = this.workflow.isActive ? this.msg("label.workflowIsInProgress") : this.msg("label.workflowIsComplete");
                Dom.get(this.id + "-statusSummary").innerHTML = $html(status);
                Dom.get(this.id + "-status").innerHTML = $html(status);

                // Load workflow's start task which "represents" the workflow
                // (if present)
                if(this.workflow.startTaskInstanceId) {
                Alfresco.util.Ajax.request(
                {
                   url: Alfresco.constants.URL_SERVICECONTEXT + "components/form",
                   dataObj:
                   {
                      htmlid: this.id + "-WorkflowForm-" + Alfresco.util.generateDomId(),
                      itemKind: "task",
                      itemId: this.workflow.startTaskInstanceId,
                      mode: "view",
                      formId: "workflow-details",
                      formUI: false
                   },
                   successCallback:
                   {
                      fn: this.onWorkflowFormLoaded,
                      scope: this
                   },
                   failureMessage: this.msg("message.failure"),
                   scope: this,
                   execScripts: true
                });
                } else {
                   this.onWorkflowFormLoaded({ serverResponse: { responseText: "<div class='form-container'><div class='form-fields'></div></div>" }});
                }
             }
        };
        
    Alfresco.component.WorkflowForm.prototype.renderCellComment= function WorkflowForm_renderCellComment(elCell, oRecord, oColumn, oData)
    {
        var Dom = YAHOO.util.Dom;

        Dom.addClass(elCell, "restore-lists")
        elCell.innerHTML = oRecord.getData("properties").bpm_comment;
    };
}
})();