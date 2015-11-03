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

if (typeof RWF == "undefined" || !RWF) {
    var RWF = {};
}

(function() {

    var $html = Alfresco.util.encodeHTML,
        $siteURL = Alfresco.util.siteURL,
        $userProfileLink = Alfresco.util.userProfileLink;

    RWF.RelatedWorkflows = function(htmlId) {
        RWF.RelatedWorkflows.superclass.constructor.call(this, "RWF.RelatedWorkflows", htmlId);
        return this;
    };

    YAHOO.extend(RWF.RelatedWorkflows, Alfresco.component.Base, {

        options: {
            relatedWorkflows: [],
            definitionsFilter: '',
            relWflPropName: '',
            curTaskId: '',
            disabled: false,
            dataSource: null,
            dataTable: null,
            dataTableHistory: null,
            selectedItems: ''
        },

        onReady: function RelWf_init() {
            this.options.definitionsFilter = this.options.definitionsFilter.replace(/\$/,"\\\$");
            if (!this.options.disabled) {
                this.fillComboBox();
                var selectWorkflowMenu = new YAHOO.widget.Button(
                    this.id + "-cntrl-workflow-start", {
                        type: "menu",
                        menu: this.id + "-cntrl-workflow-selector"
                    }
                );
                selectWorkflowMenu.getMenu().subscribe("click", this.startWorkflow, null, this);
            }
            YAHOO.Bubbling.fire('related_workflows_state_change');
            this.createDataTable();
            this.getRelatedProcesses();
            this.fillRelatedProcessesTable();
            //init assoc_packageItems
            YAHOO.Bubbling.on(
                "objectFinderReady",
                function(layer, args) {
                    var objectFinder = args[1].eventGroup;
                    if (objectFinder.options.field == "assoc_packageItems") {
                        if (objectFinder.eventGroup.indexOf(this.id) == 0) {
                            objectFinder.selectItems(this.options.selectedItems);
                        } else {
                            for(var key in objectFinder.selectedItems) {
                                if(!objectFinder.selectedItems.hasOwnProperty(key)) continue;
                                if (this.options.selectedItems) {
                                    this.options.selectedItems += ',';
                                }
                                this.options.selectedItems += objectFinder.selectedItems[key]['nodeRef'];
                            }
                        }
                    }
                },
                this
            );
        },

        getRelatedProcesses: function RelWf_getRelatedProcesses() {
            var workflows = [];
            $.ajax({
                url: Alfresco.constants.PROXY_URI +'api/related-workflows/get-by-task-id',
                type: 'GET',
                data: {
                    taskId: this.options.curTaskId.replace(/%24/g,'$'),
                    propName: this.options.relWflPropName
                },
                async: false, // :(
                success: function(json) {
                    workflows = json['workflows'];
                }
            });
            for(var i in workflows) {
                if(!workflows.hasOwnProperty(i)) continue;
                this.options.relatedWorkflows.push(workflows[i]);
            }
        },

        createDataTable: function RelWf_createDataTable() {
            this.options.dataSource = new YAHOO.util.DataSource(this.options.relatedWorkflows);
            this.options.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;

            var curTaskTableColumnDefs = [{
                key:"workflow",
                label: this.msg("related_workflows.workflow"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellWorkflow)
            }, {
                key:"task",
                label: this.msg("related_workflows.task_type"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellType)
            }, {
                key:"owner",
                label: this.msg("related_workflows.owner"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellOwner)
            }, {
                key:"dueDate",
                label: this.msg("related_workflows.dueDate"),
                sortable:true,
                resizeable:true,
                formatter: this.bind(this.renderCellDueDate)
            }, {
                key:"progress",
                label: this.msg("related_workflows.progress"),
                sortable:false,
                resizeable:true
            }, {
                key:"actions",
                label: this.msg("related_workflows.actions"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellCurrentTasksActions)
            }];
            this.options.dataTable = new YAHOO.widget.DataTable(
                this.id + "-cntrl-dataTableContainer", curTaskTableColumnDefs, this.options.dataSource, {
                    selectionMode:"single",
                    renderLoopSize: 32,
                    MSG_EMPTY: this.msg('related_workflows.no_related_workflows')
                }
            );

            var historyTaskTableColumnDefs = [{
                key:"workflow",
                label: this.msg("related_workflows.workflow"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellWorkflow)
            }, {
                key:"task",
                label: this.msg("related_workflows.task_type"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellType)
            }, {
                key:"owner",
                label: this.msg("related_workflows.owner"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellOwner)
            }, {
                key:"completionDate",
                label: this.msg("related_workflows.completion_date"),
                sortable:true,
                resizeable:true,
                formatter: this.bind(this.renderCellDateCompleted)
            }, {
                key:"progress",
                label: this.msg("related_workflows.result"),
                sortable:false,
                resizeable:true
            }, {
                key:"comment",
                label: this.msg("related_workflows.comment"),
                sortable:false,
                resizeable:true,
                formatter: this.bind(this.renderCellComment)

            }];
            this.options.dataTableHistory = new YAHOO.widget.DataTable(
                this.id + "-cntrl-dataTableHistoryContainer", historyTaskTableColumnDefs, this.options.dataSource, {
                    selectionMode:"single",
                    renderLoopSize: 32,
                    MSG_EMPTY: this.msg('related_workflows.no_related_workflows')
                }
            );
        },

        fillRelatedProcessesTable: function RelWf_fillRelatedProcessesTable() {
            this.clearTableUI();
//            this.options.relatedWorkflows.sort(
//                function(a,b) {
//                    return a.active < b.active || a.active == b.active && a.start_date > b.start_date;
//                }
//            );
            for (var i in this.options.relatedWorkflows) {
                if(!this.options.relatedWorkflows.hasOwnProperty(i)) continue;
                this.insertWorkflowIntoUI(this.options.relatedWorkflows[i]);
            }
        },

        insertWorkflowIntoUI: function RelWf_insertWorkflowIntoUI(wf) {
            var relWf = this;
            $.ajax({
                url: Alfresco.constants.PROXY_URI +'api/workflow-instances/'+wf['id']+'?includeTasks=true',
                type: 'GET',
                data: {},
                success: function(info) {
                    var wf = info['data'];
                    for (var i in wf['tasks']) {
                        if(!wf['tasks'].hasOwnProperty(i)) continue;
                        var task = wf['tasks'][i];
                        if ('COMPLETED' == task['state']) {
                            relWf.insertHistoryTaskRowIntoUI(wf, task);
                        } else {
                            relWf.insertCurTaskRowIntoUI(wf, task);
                        }
                    }
                }
            });
        },

        insertCurTaskRowIntoUI: function RelWf_insertCurTaskRowIntoUI(wf, task) {
            this.options.dataTable.addRow({
                workflow: wf,
                task: task,
                owner: task['owner'],
                dueDate: task,
                progress: task['propertyLabels']['bpm_status'],
                actions: task
            });
        },

        insertHistoryTaskRowIntoUI: function RelWf_insertHistoryTaskRowIntoUI(wf, task) {
            this.options.dataTableHistory.addRow({
                workflow: wf,
                task: task,
                owner: task['owner'],
                completionDate: task,
                progress: task['outcome'],
                comment: task
            });
        },

        clearTableUI: function RelWf_clearTableUI() {
            this.options.dataTable.getRecordSet().reset();
            this.options.dataTable.render();
            this.options.dataTableHistory.getRecordSet().reset();
            this.options.dataTableHistory.render();
        },

        renderCellType: function WorkflowForm_renderCellType(elCell, oRecord, oColumn, oData) {
            var task = oData;
            if (task.isEditable) {
                elCell.innerHTML = '<a href="' + this._getTaskUrl("task-edit", task['id']) + '" >' + $html(task['title']) + '</a>';
            } else {
                elCell.innerHTML = '<a href="' + this._getTaskUrl("task-details", task['id']) + '" >' + $html(task['title']) + '</a>';
            }
        },

        renderCellWorkflow: function WorkflowForm_renderCellWorkflow(elCell, oRecord, oColumn, oData) {
            var wf = oData;
            var displayName = wf['title'] + (wf['message']? ' "' + wf['message'] + '"' : '');
            elCell.innerHTML += '<a href="' + this._getWorkflowUrl("workflow-details", wf.id) + '" title="'+this.msg("related_workflows.workflow")+'">' + displayName + '</a>';
        },

        renderCellCurrentTasksActions: function WorkflowForm_renderCellCurrentTasksActions(elCell, oRecord, oColumn, oData) {
            var task = oData;
            elCell.innerHTML += '<a href="' + this._getTaskUrl("task-details", task.id) + '" class="related-tasks-details" title="'+this.msg("related_workflows.task_details")+'"></a>';
            if (task.isEditable) {
                elCell.innerHTML += '&nbsp;&nbsp; <a href="' + this._getTaskUrl("task-edit", task.id) + '" class="related-tasks-edit" title="'+this.msg("related_workflows.change_task")+'"></a>';
            }
        },

        renderCellComment: function WorkflowForm_renderCellComment(elCell, oRecord, oColumn, oData) {
            var comment = oData['properties']['bpm_comment'];
            if (comment !== null) {
                elCell.innerHTML = $html(comment);
            } else {
                elCell.innerHTML = '';
            }
        },

        renderCellOwner: function WorkflowForm_renderCellOwner(elCell, oRecord, oColumn, oData) {
            var owner = oData;
            if (owner != null && owner.userName) {
                var displayName = owner.firstName + ' ' + owner.lastName;
                elCell.innerHTML = $userProfileLink(
                    owner.userName,
                    owner.firstName && owner.lastName ? displayName : null,
                    null,
                    !owner.firstName
                );
            } else {
                elCell.innerHTML = '';
            }
        },

        renderCellDueDate: function WorkflowForm_renderCellDueDate(elCell, oRecord, oColumn, oData) {
            var dueISODate = oData['properties']['bpm_dueDate'];
            if (dueISODate !== null) {
                var dueDate = Alfresco.util.fromISO8601(dueISODate);
                elCell.innerHTML = Alfresco.util.formatDate(dueDate, "defaultDateOnly");
            } else {
                elCell.innerHTML = '';
            }
        },

        renderCellDateCompleted: function WorkflowForm_renderCellDateCompleted(elCell, oRecord, oColumn, oData) {
            var dueISODate = oData['properties']['bpm_completionDate'];
            if (dueISODate !== null) {
                var dueDate = Alfresco.util.fromISO8601(dueISODate);
                elCell.innerHTML = Alfresco.util.formatDate(dueDate, "defaultDateOnly");
            } else {
                elCell.innerHTML = '';
            }
        },

        fillComboBox: function RelWf_fillComboBox() {
            var selector = document.getElementById(this.id + "-cntrl-workflow-selector");
            $.ajax({
                url: Alfresco.constants.PROXY_URI + "api/related-workflows/list-definitions",
                type: 'GET',
                data: { filter: encodeURIComponent(this.options.definitionsFilter) },
                success: function(resp) {
                    for (var key in resp.data)  {
                        if(!resp.data.hasOwnProperty(key)) continue;
                        var task = resp.data[key];
                        var opt = document.createElement("option");
                        opt.appendChild(document.createTextNode(task.title));
                        opt.setAttribute("value", task.name);
                        selector.appendChild(opt);
                    }
                }
            });
        },

        startWorkflow: function RelWf_startWorkflow( p_sType, p_aArgs ) {
            var oEvent = p_aArgs[0];	//  DOM event from the menu
            var oMenuItem = p_aArgs[1];	//  Target of the event (selected workflow)
            // Get selected workflow, 'def' is workflow definition name
            var def = "";
            if (oMenuItem) {
                def = oMenuItem.value;
            } else {
                return;
            }
            var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&formId={formId}&showCancelButton=true",
                {
                    itemKind: "workflow",
                    itemId: def,
                    mode: "create",
                    submitType: "json",
                    formId: "popupDialogForm"
                });

//            var form = Alfresco.util.ComponentManager.findFirst('Alfresco.FormUI');

            // Create new dialog

            // It looks like 'destroyOnHide: true' works globally for all dialogs on the page - do not use it
            // We still delete dialog manually because we are to clear the form and everything around it
            if ( this.widgets.dialog ) {
                delete this.widgets.dialog;
            }
            this.widgets.dialog = new Alfresco.module.SimpleDialog(this.id + "-cntrl-popup-dialog");

            this.widgets.dialog.setOptions({
                width: "50em",
                templateUrl: templateUrl,
                actionUrl: null,
                destroyOnHide: false,

                // Before dialog show we just set its title
                doBeforeDialogShow: {
                    fn: function RelWf_customizeDialogProperties(p_form, p_dialog) {
                        Alfresco.util.populateHTML([
                            p_dialog.id + "-dialogTitle",
                            Alfresco.util.message(this.msg("related_workflows.new_workflow_dialog_title"))
                        ]);
                    },
                    scope: this
                },

                // It is called when dialog is closed with success.
                // It means child workflow was started successfully and we got the response.
                onSuccess: {
                    fn: function RelWf_dialog_on_success(response, p_obj) {
                        // Get workflow description from the form
                        var desc = response.config.dataObj.prop_bpm_workflowDescription;
                        var id = response.json.persistedObject.replace('WorkflowInstance[id=','').split(',')[0];
                        // Save workflow details, update server state and UI
                        this.commitCreatedWorkflow(desc, id);
//                        form.formsRuntime._runValidations(true);
                    },
                    scope: this
                },

                onFailure: {
                    fn: function RelWf_dialog_on_failure(response) {
                        // Do nothing
                    },
                    scope: this
                }
            }).show();
        },

        commitCreatedWorkflow: function RelWf_commitCreatedWorkflow(desc, id) {
            var curRelatedWorkflows = document.getElementById(this.id);
            if (curRelatedWorkflows.value == '') {
                curRelatedWorkflows.value = id;
            } else {
                curRelatedWorkflows.value += ',' + id;
            }
            var form = this._getFormEl();
            var alf_form = new Alfresco.forms.Form(form.id);
            alf_form.init();
            alf_form.setAJAXSubmit(true);
            alf_form.setSubmitAsJSON(true);
            alf_form.setAjaxSubmitMethod("POST");
            // Save current form - save relations on the server
            var ev = document.createEvent('KeyboardEvent');
            // Hack to make it cross-browser
            if(ev.initKeyboardEvent) {
                ev.initKeyboardEvent("keyup", true, true, window, false, false, false, false, 0, 32);
            } else {
                ev.initKeyEvent("keyup", true, true, window, false, false, false, false, 0, 32);
            }
            alf_form._submitInvoked(ev);

            document.location.reload();
        },

        _getFormEl: function RelWf_getFormEl() {
            var dataField = document.getElementById(this.id);
            var el = dataField;
            while (el.tagName != 'FORM') {
                el = el.parentElement;
            }
            return el;
        },

        _getTaskUrl: function WF___getReferrer(page, taskId) {
            var url = page + "?taskId=" +encodeURIComponent(taskId);
            if (this.options.referrer) {
                url += "&referrer=" + encodeURIComponent(this.options.referrer);
            } else if (this.options.nodeRef) {
                url += "&nodeRef=" + encodeURIComponent(this.options.nodeRef);
            }
            return $siteURL(url);
        },

        _getWorkflowUrl: function WF___getReferrer(page, workflowId) {
            var url = page + "?workflowId=" + encodeURIComponent(workflowId);
            if (this.options.referrer) {
                url += "&referrer=" + encodeURIComponent(this.options.referrer);
            }
            return $siteURL(url);
        }


    });

})();
