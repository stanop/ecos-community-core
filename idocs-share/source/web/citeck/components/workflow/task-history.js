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

if (typeof Citeck == "undefined" || !Citeck) {
    var Citeck = {};
}

(function() {

    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML,
        $siteURL = Alfresco.util.siteURL,
        $userProfileLink = Alfresco.util.userProfileLink;


    Citeck.HistoryTasks = function HT_constructor(htmlId) {
        Citeck.HistoryTasks.superclass.constructor.call(this, "Citeck.HistoryTasks", htmlId);

        YAHOO.Bubbling.on("taskDetailedData", this.onTaskDetailsData, this);
        return this;
    };

    YAHOO.extend(Citeck.HistoryTasks, Alfresco.component.Base, {

        options: {
            enable: null
        },

        task: null,
        workflow: null,
        historyTasks: [],


        onTaskDetailsData: function TDH_onTaskDetailsData(layer, args) {
            this.task = args[1];
            var workflowId = this.task.workflowInstance.id;
            if (this.options.enable) {
                var workflowInstancesUrl = YAHOO.lang.substitute(
                    Alfresco.constants.PROXY_URI + "api/workflow-instances/{workflowId}?includeTasks=true", {
                        workflowId: encodeURIComponent(workflowId)
                    }
                );
                YAHOO.util.Connect.asyncRequest(
                    'GET',
                    workflowInstancesUrl, {
                        success: function (o) {
                            if (o.responseText) {
                                this.workflow = YAHOO.lang.JSON.parse(o.responseText)['data'];
                                this.loadTasks(this.workflow);
                                this.printTasks();
                            }
                        },
                        failure: this.handleFailure,
                        scope: this
                    }
                );
            }
        },

        loadTasks: function HT_loadTasks(workflow) {
            var tasks = workflow.tasks, recentTask;
            for (var i = 0, il = tasks.length; i < il; i++) {
                if (tasks[i].state == "COMPLETED") {
                    this.historyTasks.push(tasks[i]);
                }
            }
            var sortByDate = function(dateStr1, dateStr2) {
                var date1 = Alfresco.util.fromISO8601(dateStr1),
                    date2 = Alfresco.util.fromISO8601(dateStr2);
                if (date1 && date2) {
                    return date1 < date2 ? 1 : -1;
                } else {
                    return !date1 ? 1 : -1;
                }
            };
            this.historyTasks.sort(function(task1, task2) {
                return sortByDate(task1.properties.bpm_completionDate, task2.properties.bpm_completionDate);
            });
        },

        printTasks: function HT_printTasks() {
            var historyColumnDefinitions = [
                { key: "name", label: this.msg("column.type"), formatter: this.bind(this.renderCellType) },
                { key: "owner", label: this.msg("column.completedBy"), formatter: this.bind(this.renderCellCompletedBy) },
                { key: "id", label: this.msg("column.dateCompleted"), formatter: this.bind(this.renderCellDateCompleted) },
                { key: "state", label: this.msg("column.outcome"), formatter: this.bind(this.renderCellOutcome) },
                { key: "properties", label: this.msg("column.comment"), formatter: this.bind(this.renderCellComment) }
            ];
            // Create header and data table elements
            var historyContainerEl = Dom.get(this.id + "-tasksHistory-form-section"),
                historyTasksEl = Selector.query("div", historyContainerEl, true);

            // Create workflow history data table filled with history tasks
            var workflowHistoryDS = new YAHOO.util.DataSource(
                this.historyTasks, {
                    responseType: YAHOO.util.DataSource.TYPE_JSARRAY
                }
            );
            this.widgets.historyTasksDataTable = new YAHOO.widget.DataTable(
                historyTasksEl,
                historyColumnDefinitions,
                workflowHistoryDS, {
                    MSG_EMPTY: this.msg("label.noTasks")
                }
            );
        },

        onReady: function HT_onReady() {
            if (this.options.enable) {
                $('#' + this.id + '-tasksHistory').removeClass('hidden');
            }
        },

        renderCellType: function HT_renderCellType(elCell, oRecord, oColumn, oData) {
            var task = oRecord.getData();
            if (task.isEditable) {
                elCell.innerHTML = '<a href="' + this._getTaskUrl("task-edit", oRecord.getData("id")) + '" title="' + this.msg("link.title.task-edit") + '">' + $html(oRecord.getData("title")) + '</a>';
            } else {
                elCell.innerHTML = '<a href="' + this._getTaskUrl("task-details", oRecord.getData("id")) + '" title="' + this.msg("link.title.task-details") + '">' + $html(oRecord.getData("title")) + '</a>';
            }
        },

        renderCellCompletedBy: function HT_renderCellCompletedBy(elCell, oRecord, oColumn, oData) {
            var status = oRecord.getData("properties").bpm_status;
            // Value based on list 'bpm:allowedStatus' in bpmModel.xml
            if(status != null && status != "Completed") {
                elCell.innerHTML = this.msg("label.none");
            } else {
                this.renderCellOwner(elCell, oRecord, oColumn, oData);
            }
        },

        renderCellDateCompleted: function HT_renderCellDateCompleted(elCell, oRecord, oColumn, oData) {
            var completionDate = Alfresco.util.fromISO8601(oRecord.getData("properties").bpm_completionDate);
            elCell.innerHTML = Alfresco.util.formatDate(completionDate);
        },

        renderCellOutcome: function HT_renderCellOutcome(elCell, oRecord, oColumn, oData) {
            elCell.innerHTML = $html(oRecord.getData("outcome"));
        },

        renderCellComment: function HT_renderCellComment(elCell, oRecord, oColumn, oData) {
            elCell.innerHTML = $html(oRecord.getData("properties").bpm_comment);
        },

        renderCellOwner: function HT_renderCellOwner(elCell, oRecord, oColumn, oData) {
            var owner = oRecord.getData("owner");
            if (owner != null && owner.userName) {
                var displayName = $html(this.msg("field.owner", owner.firstName, owner.lastName));
                elCell.innerHTML = $userProfileLink(owner.userName, owner.firstName && owner.lastName ? displayName : null, null, !owner.firstName);
            } else {
                elCell.innerHTML = this.msg("label.none");
            }
        },

        _getTaskUrl: function HT_getReferrer(page, taskId) {
            var url = page + "?taskId=" + encodeURIComponent(taskId);
            if (this.options.referrer) {
                url += "&referrer=" + encodeURIComponent(this.options.referrer);
            } else if (this.options.nodeRef) {
                url += "&nodeRef=" + encodeURIComponent(this.options.nodeRef);
            }
            return $siteURL(url);
        },

        handleFailure: function Grid_handleFailure(o){
            if (o.responseText) {
                alert(o.responseText);
            }
        }

    });

})();