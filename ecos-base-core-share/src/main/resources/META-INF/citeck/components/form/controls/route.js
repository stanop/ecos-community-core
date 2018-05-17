/*
 * Copyright (C) 2008-2016 Citeck LLC.
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


    if(typeof Citeck == "undefined") Citeck = {};
    if(typeof Citeck.widget == "undefined") Citeck.widget = {};

    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;

    var $html = Alfresco.util.encodeHTML,
        $combine = Alfresco.util.combinePaths;

    Citeck.widget.Route = function(htmlId) {
        Citeck.widget.Route.superclass.constructor.call(this, "Citeck.widget.Route", htmlId);
    };

    YAHOO.extend(Citeck.widget.Route, Alfresco.component.Base, {

        //  DEFAULT OPTIONS
        options: {
            routeObjectFieldId: null,
            priorityFieldId: null,
            mode: "create",

            saveAndLoadTemplate: "false",

            presetTemplate: null,
            presetTemplateMandatory: "false",

            participantListFieldId: null,

            mandatory: "false"
        },

        onReady: function() {
            var self = this;

            this.field = Dom.get(this.id);
            this.elements = {
                routeObject: Dom.get(this.options.routeObjectFieldId),
                priority: Dom.get(this.options.priorityFieldId),
                participantList: Dom.get(this.options.participantListFieldId),

                stages: Dom.getElementsByClassName("stages", "table", this.field)[0],
            }

            this.routes = [];
            this.stages = [];

            this.permissions = {
                canCreate: false
            }

            this.fn = {
                viewRender: function(stages) {
                    for (var s in stages) {
                        var participants = stages[s].participants;

                        var trStage = $("<tr/>", { "class": "stage", id: "stage-" + (+s + 1) }),
                            ulParticipants = $("<ul/>", { "class": "participants" });

                        for (var p in participants) {
                            var authority = participants[p].authority;
                            if (authority) {
                                var displayName;
                                if (authority.displayName) {
                                    displayName = authority.displayName;
                                } else if (authority.authorityDisplayName) {
                                    displayName = authority.authorityDisplayName;
                                } else if (authority.firstName && authority.lastName) {
                                    displayName = authority.firstName + " " + authority.lastName;
                                } else {
                                    displayName = authority.name;
                                }

                                ulParticipants
                                    .append($("<li/>", { "class": "participant", html: displayName }));
                            }
                        }

                        var dueDateExpr = stages[s].dueDateExpr,
                            time, timeType;
                        if (dueDateExpr) {
                            time = dueDateExpr.split("/")[0];
                            timeType = dueDateExpr.split("/")[1];

                            if (time) {
                                switch(timeType) {
                                    case "h":
                                        time += " " + self.msg("route.hours");
                                        break;
                                    case "d":
                                        time += " " + self.msg("route.days");
                                        break;
                                    case "m":
                                        time += " " + self.msg("route.months");
                                        break;
                                }
                            }
                        }

                        trStage
                            .append($("<td/>", { "class": "stage-number", html: (+s + 1) + self.msg("route.of-stage") }))
                            .append($("<td/>", { "class": "stage-time", html: time ? time : null }))      
                            .append($("<td/>").append(ulParticipants));

                        $("tbody", self.elements.stages)
                            .append(trStage);
                    }
                },

                editRender: function(stages, options) {
                    for (var s in stages) {
                        if (options && options.mandatory) {
                            self.createStage(self.elements.stages, stages[s], options.mandatory);
                        } else {
                            self.createStage(self.elements.stages, stages[s]);
                        }
                    }
                }
            }

            if (this.options.mode == "view" || this.options.mode == "edit") {
                this.elements.itemId = Dom.get("nodeRefItemId");

                if (this.elements.itemId) {
                    var itemId = this.elements.itemId.value;
                    if (itemId) {
                        Alfresco.util.Ajax.jsonGet({
                            url: Alfresco.constants.PROXY_URI + "api/citeck/routes?nodeRef=" + itemId,
                            successCallback: {
                                scope: this,
                                fn: function(response) {
                                    var result = response.json,
                                        route = result.data[0],
                                        stages = [];

                                    if (route) {
                                        stages = route.stages;
                                    } else {
                                        var routeObject = this.elements.routeObject;
                                        if (routeObject && routeObject.value) {
                                            stages = JSON.parse(routeObject.value);
                                        }
                                    }

                                    if (this.options.mode == "view") {
                                        this.fn.viewRender(stages);
                                    } 

                                    if (this.options.mode == "edit") {
                                        this.fn.editRender(stages);
                                    }

                                    var scope = this;
                                    setTimeout(function() { 
                                        scope.onUpdate();
                                    }, 250);
                                }
                            },
                            failureCallback: {
                                scope: this,
                                fn: function(response) {
                                    console.log("error")
                                }
                            }
                        });
                    } 
                } else {
                    if (this.elements.routeObject && this.elements.routeObject.value) {
                        var stages = JSON.parse(this.elements.routeObject.value);

                        if (this.options.mode == "edit") {
                            this.fn.editRender(stages);
                        }

                        if (this.options.mode == "view") {
                            this.fn.viewRender(stages);
                        }
                    }
                }
            }

            if (this.options.mode == "create" || this.options.mode == "edit") {
                // initialize main yui buttons
                this.buttons = {
                    addParticipant: [],
                    addStage: new YAHOO.widget.Button(Dom.getElementsByClassName("addStage", "button", this.field)[0])
                }

                // initialize dialogs and buttons for them if property 'templateButtons' id true
                if (this.options.saveAndLoadTemplate == "true") {
                    this.dialogs = {
                        saveAsTemplate: new YAHOO.widget.Dialog("saveAsTemplatePanel", {
                            width: 500,
                            fixedcenter: true,
                            constraintoviewport: true,
                            close: true,
                            visible: false,
                            draggable: false,
                            model: true
                        }),
                        loadTemplate: new YAHOO.widget.Dialog("loadTemplatePanel", {
                            width: 500,
                            fixedcenter: true,
                            constraintoviewport: true,
                            close: true,
                            visible: false,
                            draggable: false,
                            model: true
                        })
                    };

                    // configure saveAsTemplateDialog
                    this.dialogs.saveAsTemplate.cfg.queueProperty("buttons", [{ text: self.msg("route.save"), isDefault: true }, { text: self.msg("route.cancel") }]);
                    this.dialogs.saveAsTemplate.render(document.body);

                    // configure loadTemplate
                    this.dialogs.loadTemplate.cfg.queueProperty("buttons", [{ text: self.msg("route.load"), isDefault: true }, { text: self.msg("route.cancel") }]);
                    this.dialogs.loadTemplate.render(document.body);

                    var saveTemplateButtons = this.dialogs.saveAsTemplate.getButtons(),
                        loadTemplateButtons = this.dialogs.loadTemplate.getButtons();

                    // initialize yui buttons for dialog's buttons
                       
                    this.buttons["saveAsTemplateDialog"] = new YAHOO.widget.Button(Dom.getElementsByClassName("saveAsTemplateDialog", "button", this.field)[0]);
                    this.buttons["loadTemplateDialog"] = new YAHOO.widget.Button(Dom.getElementsByClassName("loadTemplateDialog", "button", this.field)[0]);

                    // saveAsTemplateButtons
                    this.buttons["saveAsTemplateSubmit"] = saveTemplateButtons[0];
                    this.buttons["saveAsTemplateCancel"] = saveTemplateButtons[1];

                    // loadTemplateButtons
                    this.buttons["loadTemplateSubmit"] = loadTemplateButtons[0];
                    this.buttons["loadTemplateCancel"] = loadTemplateButtons[1];

                    // saveAsTemplate button events
                    this.buttons.saveAsTemplateDialog.on("click", this.dialogs.saveAsTemplate.show, {}, this.dialogs.saveAsTemplate);
                    this.buttons.saveAsTemplateSubmit.on("click", this.saveAsTemplate, {}, this);
                    this.buttons.saveAsTemplateCancel.on("click", this.dialogs.saveAsTemplate.cancel, {}, this.dialogs.saveAsTemplate);

                    // loadTemplate button events
                    this.buttons.loadTemplateDialog.on("click", this.showLoadTemplate, {}, this);
                    this.buttons.loadTemplateSubmit.on("click", this.loadTemplate, {}, this);
                    this.buttons.loadTemplateCancel.on("click", this.dialogs.loadTemplate.cancel, {}, this.dialogs.loadTemplate);
                }

                this.buttons.addStage.on("click", this.onAddStage, {}, this);
            }

            // prepare first stage template
            if (this.options.mode == "create") {
                if (this.options.presetTemplate) {
                    Alfresco.util.Ajax.jsonGet({
                        url: Alfresco.constants.PROXY_URI + "api/citeck/routes?nodeRef=" + this.options.presetTemplate,
                        successCallback: {
                            scope: this,
                            fn: function(response) {
                                var result = response.json,
                                route = result.data[0],
                                stages = [];

                                if (route) {
                                    stages = route.stages;
                                    this.fn.editRender(stages, { mandatory: this.options.presetTemplateMandatory })
                                } 

                                var scope = this;
                                setTimeout(function() { 
                                    scope.onUpdate();
                                }, 250);
                            }
                        },
                        failureCallback: {
                            scope: this,
                            fn: function(response) {
                                console.log("error")
                            }
                        }
                    });
                } else {
                    this.createStage(this.elements.stages);
                }
            }

            // check permissions
            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "api/citeck/routes?onlyPermissions=true",
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var result = response.json;
                        this.permissions.canCreate = result.canCreate;

                        // show saveAsTemplate if permission 'canCreate' is true
                        if (result.canCreate && this.options.saveAndLoadTemplate == "true") this.buttons.saveAsTemplateDialog.removeClass("hidden");
                    }
                },
                failureCallback: {
                    scope: this,
                    fn: function(response) {
                        console.log("error")
                    }
                }
            });
            
            // hide saveAsTemplate if permission 'canCreate' is FALSE
            if (!this.permissions.canCreate && this.options.saveAndLoadTemplate == "true") this.buttons.saveAsTemplateDialog.addClass("hidden");
        },

        setRoute: function(nodeRef, mandatory) {
            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "api/citeck/routes?nodeRef=" + nodeRef,
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var result = response.json,
                        route = result.data[0]

                        if (route) {
                            this.clearStages(this.elements.stages, this.stages);
                            this.fn.editRender(route.stages, { mandatory: mandatory.toString() });
                            this.stages = route.stages;
                        }

                        var scope = this;
                        setTimeout(function() { 
                            scope.onUpdate();
                        }, 250);
                    }
                },
                failureCallback: {
                    scope: this,
                    fn: function(response) {
                        console.log("error")
                    }
                }
            });
        },

        hasRoute: function() {
            return this.stages ? true : false;
        },

        saveAndLoadTemplateVisibility: function(visibility) {
            if (!visibility) {
                throw new Error("The argument can not be empty. 'visible' or 'hidden' only");
                return;
            }

            if (this.options.saveAndLoadTemplate == "false") {
                throw new Error("The property 'saveAndLoadTemplate' is false. Can not show or hide dialogs");
                return;
            }

            if (this.dialogs && this.buttons) {
                if (this.buttons.saveAsTemplateDialog && this.buttons.loadTemplateDialog) {
                    switch (visibility) {
                        case "hidden":
                            // hide saveAsTemplate button only if permission 'canCreate' is TRUE
                            if (this.permissions.canCreate) { this.buttons.saveAsTemplateDialog.addClass("hidden"); }
                            this.buttons.loadTemplateDialog.addClass("hidden");
                            break
                        case "visible":
                            // visible saveAsTemplate button only if permission 'canCreate' is TRUE
                            if (this.permissions.canCreate) { this.buttons.saveAsTemplateDialog.removeClass("hidden"); }
                            this.buttons.loadTemplateDialog.removeClass("hidden");
                            break
                    }
                }
            }
        },

        onUpdate: function(event) {
            var participants = [],
                stages = [];

            // update priority field
            $("tbody.stage", this.elements.stages)
                .each(function() {
                    var stageParticipants = [];
                    var dueDateExpr;
                    $("input[type=\"text\"].time", this)
                        .each(function() {
                            var str = this.value.replace(/\s+/,''); //remove spaces
                            if (str.length == 0) {
                                dueDateExpr = "0/";
                            } else {
                                dueDateExpr = str + "/";
                            }
                        });
                    $("select.time-type", this)
                        .each(function() {
                            if (this.value) {
                                dueDateExpr = dueDateExpr + this.value;
                            }
                        });
                    $("input[type=\"hidden\"].participant", this)
                        .each(function() {
                            if (this.value) {
                                participants.push(this.value);
                                stageParticipants.push(this.value);
                            }
                        });
                if (stageParticipants.length > 0) {
                    stageParticipants[0] = stageParticipants[0] + "_" + dueDateExpr;
                }
                stages.push(stageParticipants.join("|"));
            });

            this.elements.priority.value = stages.join(",");


            // update route field
            if (this.elements.routeObject) {
                this.elements.routeObject.value = JSON.stringify(this.buildStagesArray(this));
            }

            // update participantList field
            if (this.elements.participantList) {
                this.elements.participantList.value = participants;
            }
            if (this.options.mandatory) {
                YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
            }
        },

        onDeleteStage: function(event, object) {
            var self = this,
                stageId = object.deleteButton._button.id.replace("-deleteButton-button", "");

            // delete stage from dom and this.stages
            $("tbody#" + stageId, this.elements.stages).remove();
            delete self.stages[stageId];

            // right ids in elements after delete
            $("tbody.stage", this.elements.stages)
                .each(function(index) {
                    var stageNumber = index + 1,
                        newStageId = "stage-" + stageNumber,
                        oldStageId = this.id;

                    if (oldStageId != newStageId) {
                        self.stages[newStageId] = self.stages[oldStageId];
                        delete self.stages[oldStageId];

                        var stageObject = self.stages[newStageId],
                            selectPickers = self.stages[newStageId].participants;

                        for (var i in selectPickers) {
                            var selectPicker = selectPickers[i],
                                elements = selectPicker.elements;

                            selectPicker.id = selectPicker.id.replace(oldStageId, newStageId);
                            selectPicker.field.id = selectPicker.field.id.replace(oldStageId, newStageId);

                            for (var key in elements) {
                                var element = elements[key];

                                if (element.id && element.id.indexOf(oldStageId) != -1) {
                                    element.id = element.id.replace(oldStageId, newStageId);
                                }
                            }
                        }

                        // rename tbody id
                        this.id = newStageId;

                        // rename deleteButton
                        $("tr.stage-managment span.yui-button", this).attr("id", newStageId + "-deleteButton");
                        $("tr.stage-managment span.yui-button button", this).attr("id", newStageId + "-deleteButton-button");

                        // rename addParticipantButton
                        $("tr.stage-buttons span.yui-button", this).attr("id", newStageId + "-addParticipant");
                        $("tr.stage-buttons span.yui-button button", this).attr("id", newStageId + "-addParticipant-button");

                        // rename header
                        $("tr.headers th:first-child", this).html(stageNumber + self.msg("route.of-stage"));

                        // rename ids for stage time field
                        $("td.stage-time input.time", this).attr("id", "stage-" + stageNumber + "-time");
                        $("td.stage-time select.time-type", this).attr("id", "stage-" + stageNumber + "-time-type");
                    }
                })

            this.onUpdate();
        },

        onAddParticipant: function(event, object) {
            var stageId = object.addButton._button.id.replace("-addParticipant-button", ""),
                stageObj = this.stages[stageId],
                participantId = stageId + "-participant-" + (stageObj.participants.length + 1),
                newParticipant = $("<input/>", { type: "hidden", "class": "participant", id: participantId }),
                deleteButton = $("<button/>", { 
                    "class": "deleteParticipant", 
                    id: participantId + "-deleteButton",
                    html: "x" 
                });

            $("#"+ stageId + " .stage-participants")
                .append(newParticipant)
                .append(deleteButton);

            var deleteYUIButton = new YAHOO.widget.Button(deleteButton[0]);
            deleteYUIButton.on("click", this.onDeleteParticipant, { deleteButton: deleteYUIButton }, this);
            stageObj.deleteParticpantButtons.push(deleteYUIButton);

            var optionsForSelectPicker = {
                nodeSelectCallback: this.onParticipantNodeSelect,
                nodeSelectConstraintCallback: this.onParticipantNodeSelectConstraint, 
                context: this
            }

            if (this.options.allowedAuthorityType) {
                optionsForSelectPicker.allowedAuthorityType = this.options.allowedAuthorityType;
            }

            if (this.options.allowedGroupType) {
                optionsForSelectPicker.allowedGroupType = this.options.allowedGroupType;
            }


            stageObj.participants.push(new Citeck.widget.SelectPicker(newParticipant.attr("id")).setOptions(optionsForSelectPicker));
        },

        onDeleteParticipant: function(event, object) {
            var self = this,
                participantId = object.deleteButton._button.id.replace("-deleteButton-button", ""),
                stageId = participantId.split("-participant-")[0],
                stage = $("tbody#" + stageId),
                participantsSelectPicker = self.stages[stageId].participants;

            if (participantsSelectPicker.length > 1) {
                // delete from dom
                $("div#" + participantId + "-selectPickerBox + .yui-button", stage).remove();
                $("div#" + participantId + "-selectPickerBox", stage).remove();

                // delete from this.stages.participants
                for (var i in participantsSelectPicker) {
                    if (participantsSelectPicker[i].id == participantId) {
                        participantsSelectPicker.splice(i, 1);
                    }
                }

                // right ids in elements after delete
                $(".stage-participants div.select-picker-box", stage)
                    .each(function(index) {
                        var newParticipantId = stageId + "-participant-" + (+index + 1),
                            oldParticipantId = this.id.replace("-selectPickerBox", "");

                        if (oldParticipantId != newParticipantId) {
                            for (var i in participantsSelectPicker) {
                                var selectPicker = participantsSelectPicker[i];

                                if (selectPicker.id == oldParticipantId) {
                                    var elements = selectPicker.elements;

                                    selectPicker.id = newParticipantId;
                                    selectPicker.field.id = newParticipantId;

                                    for (var key in elements) {
                                        var element = elements[key];

                                        if (element.id && element.id.indexOf(oldParticipantId) != -1) {
                                            element.id = element.id.replace(oldParticipantId, newParticipantId);
                                        }
                                    }
                                }
                            }

                            // rename deleteButton
                            var button = $(this).next();
                            button.attr("id", newParticipantId + "-deleteButton");
                            $("button", button).attr("id", newParticipantId + "-deleteButton-button");
                        }
                    });
            } else if (participantsSelectPicker.length == 1) {
                participantsSelectPicker[0].restoreByDefaults();
            }
            
            this.onUpdate();
        },

        onParticipantNodeSelect: function(args, context) {
            context.onUpdate();
        },

        onParticipantNodeSelectConstraint: function(node, context) {
            if (context.elements.participantList.value.indexOf(node.data.nodeRef) == -1) return true;
            return false;
        },

        onAddStage: function(event) {
            this.createStage(this.elements.stages);
        },


        saveAsTemplate: function(event) {
            var dialog = this.dialogs.saveAsTemplate,
                routeName = $("input[id=\"routeName\"]", dialog.body).val();

            dialog.hide();

            if (routeName) {
                var route = this.buildRouteObject(routeName, this);
                
                Alfresco.util.Ajax.jsonPost({
                    url: Alfresco.constants.PROXY_URI + "api/citeck/routes",
                    dataObj: route,
                    successCallback: {
                        scope: this,
                        fn: function(response) {
                            Alfresco.util.PopupManager.displayMessage({ 
                                text: this.msg("route.template.saved-success") 
                            })
                        }
                    },
                    failureCallback: {
                        scope: this,
                        fn: function(response) {
                            Alfresco.util.PopupManager.displayMessage({ 
                                text: this.msg("route.template.saved-failure") 
                            })
                        }
                    }
                });
            }
        },

        showLoadTemplate: function(event) {
            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "api/citeck/routes",
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var dialog = this.dialogs.loadTemplate,
                            select = $("select#routeTemplate", dialog.body),
                            routes = response.json.data;

                        if (routes.length > 0) {
                            this.routes = routes;
                            select.html("")
                            for (var i in routes) {
                                select.append($("<option>", { value: routes[i].nodeRef, html: routes[i].name }))
                            }
                        }

                       dialog.show();
                    }
                },
                failureCallback: {
                    scope: this,
                    fn: function(response) {
                        console.log("FAIL")
                    }
                }
            });
        },

        loadTemplate: function(event) {
            var dialog = this.dialogs.loadTemplate,
                selectedRouteNodeRef = $("select#routeTemplate", dialog.body).val(),
                selectedRouteObject = {};

            dialog.hide();

            for (var i in this.routes) {
                if (this.routes[i].nodeRef == selectedRouteNodeRef) {
                    selectedRouteObject = this.routes[i];
                    break;
                }
            }

            if (selectedRouteObject) {
                this.clearStages(this.elements.stages, this.stages);
                this.fn.editRender(selectedRouteObject.stages, false)
            }

            this.onUpdate();
        },



        //                   //
        // PRIVATE FUNCTIONS //
        //                   //

        buildStagesArray: function(scope) {
            var array = [];

            for (var key in scope.stages) {
                var stage = scope.stages[key],
                    participantsEls = stage.participants;

                var stageObj = {
                    position: stage.position, 
                    participants: [] 
                };

                if (stage.stageTime && stage.stageTime.timeType && stage.stageTime.time) {
                    var timeType = $(stage.stageTime.timeType).val(),
                        time = $(stage.stageTime.time).val();
                    stageObj.dueDateExpr = time + "/" + timeType;
                }

                if (stage.nodeRef)
                    stageObj.nodeRef = stage.nodeRef;

                for (var p in participantsEls) {
                    var participantInput = participantsEls[p].field;
                    if (participantInput && participantInput.value) {
                        stageObj.participants.push({
                            position: participantInput.id.replace("stage-" + stage.position + "-participant-", ""),
                            authority: {
                                nodeRef: participantInput.value,
                                displayName: participantInput.previousSibling.previousSibling.text
                            }
                            
                        })
                    }
                }

                array.push(stageObj);              
            }

            return array;
        },

        buildRouteObject: function(name, scope) {
            var obj = {
                    route: {
                        name: name,
                        stages: []
                    }
                };  

            for (var key in scope.stages) {
                var stage = scope.stages[key],
                    participantsEls = stage.participants;

                var timeType = $(stage.stageTime.timeType).val(),
                    time = $(stage.stageTime.time).val()

                var stageObj = {
                        dueDateExpr: time + "/" + timeType,
                        position: stage.position, 
                        participants: [] 
                    };

                for (var p in stage.participants) {
                    var participantInput = stage.participants[p].field;
                    if (participantInput && participantInput.value) {
                        stageObj.participants.push({
                            position: participantInput.id.replace("stage-" + stage.position + "-participant-", ""),
                            nodeRef: participantInput.value
                        })
                    }
                }

                obj.route.stages.push(stageObj);              
            }

            return obj;
        },

        clearStages: function(stageContainer, stageObject) {
            stageContainer.innerHTML = "";
            stageObject = {};
        },

        createStage: function(stageContainer, templateObj, mandatory) {
            var stageNumber = stageContainer.tBodies.length + 1,
                stageId = "stage-" + stageNumber,
                stageTitle = stageNumber  + this.msg("route.of-stage"),
                dueDateExpr, nodeRef,
                participants = [{  position: 1, id: stageId + "-participant-1" }],
                participantsSelectPicker = [],
                participantsDeleteButtons = [];

            mandatory = mandatory ? mandatory.toLowerCase() == "true" : false;

            // if available template object
            if (templateObj) {
                if (templateObj.dueDateExpr) {
                    dueDateExpr = templateObj.dueDateExpr.split("/");
                }

                if (templateObj.nodeRef) {
                    nodeRef = templateObj.nodeRef;
                }

                participants = templateObj.participants;

                // sort participants by position if more then 1
                if (participants.length > 1) {
                    participants = participants.sort(function(a, b) { return +a.position > +b.position });
                }

                for (var i in participants) {
                    participants[i]["id"] = stageId + "-participant-" + participants[i].position;
                }
            } else {
                participants[0]["deleteButton"] = $("<button/>", { 
                    "class": "deleteParticipant",
                    id: stageId + "-participant-1-deleteButton", 
                    html: "x" 
                });
            }

            var tbodyStage = $("<tbody/>", { "class": "stage", id: stageId }),
                trManagment = $("<tr/>", { "class": "stage-managment", html: "<td colspan=\"2\"></td>" }),
                trHead = $("<tr/>", { "class": "headers" }),
                trBody = $("<tr/>"),
                tdStageTime = $("<td/>", { "class": "stage-time" }),
                inputDuoDateExpr = $("<input/>", { type: "text", "class": "time", id: stageId + "-time", value: dueDateExpr ? dueDateExpr[0] : null }),
                divTimeControl = $("<div/>", { "class": "time-control" }),
                selectTimeType = $("<select/>", { "class": "time-type", id: stageId + "-time-type" }),
                tdStageParticipants = $("<td/>", { "class": "stage-participants" }),
                trParticipantButtons = $("<tr/>", { "class": "stage-buttons" }),
                buttonAddParticipant = $("<button/>", { 
                    "class": "addParticipant",
                    id: stageId + "-addParticipant",
                    html: "+" });

            if (!mandatory) {
                var buttonDeleteStage = $("<button/>", { 
                    "class": "deleteStage", 
                    id: stageId + "-deleteButton", 
                    html: this.msg("route.delete-stage") 
                });

                $("td", trManagment)
                    .first()
                    .append(buttonDeleteStage);

                var stageButton = new YAHOO.widget.Button(buttonDeleteStage[0], { disabled: mandatory });
                stageButton.on("click", this.onDeleteStage, { deleteButton: stageButton }, this);

                tbodyStage.append(trManagment);
            }


            $([[this.msg("route.hours"), "h"], [this.msg("route.days"), "d"], [this.msg("route.months"), "m"]]).each(function() {
                selectTimeType.append($("<option/>", { value: this[1], text: this[0], selected: dueDateExpr ? dueDateExpr[1] == this[1] : false }));
            });

            Event.addListener(selectTimeType[0], "change", this.onUpdate, this, true);
            Event.addListener(inputDuoDateExpr[0], "change", this.onUpdate, this, true);

            trHead
                .append($("<th/>", { html: stageTitle }))
                .append($("<th/>", { html: this.msg("route.participants") + "<span class=\"mandatory-indicator\">*</span>" }));

            divTimeControl
                .append(inputDuoDateExpr)
                .append(selectTimeType);

            tdStageTime
                .append($("<div/>", { "class": "time-title", html: this.msg("route.time") }))
                .append(divTimeControl);

            for (var i in participants) {
                var deleteButton = $("<button/>", { 
                    "class": "deleteParticipant", 
                    id: stageId + "-participant-" + (+i + 1) + "-deleteButton",
                    html: "x" 
                });

                tdStageParticipants.append($("<input/>", { 
                    type: "hidden", 
                    "class": "participant", 
                    id: stageId + "-participant-" + participants[i].position, 
                    value: participants[i].authority ? participants[i].authority.nodeRef : null
                }));
                    
                if (!mandatory) {
                    tdStageParticipants.append(deleteButton);
                    participants[i]['deleteButton'] = deleteButton[0];
                }
            }

            trBody
                .append(tdStageTime)
                .append(tdStageParticipants);

            trParticipantButtons
                .append($("<td/>"))
                .append($("<td/>").append(buttonAddParticipant));

            tbodyStage
                .append(trHead)
                .append(trBody)
                .append(trParticipantButtons)
                .append($("<tr/>", { "class": "separator", html: "<td colspan=\"2\"><div></div></td>" }));

            $(stageContainer).append(tbodyStage);

            addParticipantYUIButton = new YAHOO.widget.Button(buttonAddParticipant[0]);
            addParticipantYUIButton.on("click", this.onAddParticipant, { addButton: addParticipantYUIButton }, this);

            for (var i in participants) {
                var optionsForSelectPicker = {
                    disabled: mandatory,
                    selectedNode: participants[i].authority ? participants[i].authority : null,
                    nodeSelectCallback: this.onParticipantNodeSelect,
                    nodeSelectConstraintCallback: this.onParticipantNodeSelectConstraint, 
                    context: this 
                }

                if (this.options.allowedAuthorityType) {
                    optionsForSelectPicker.allowedAuthorityType = this.options.allowedAuthorityType;
                }

                if (this.options.allowedGroupType) {
                    optionsForSelectPicker.allowedGroupType = this.options.allowedGroupType;
                }

                participantsSelectPicker.push(new Citeck.widget.SelectPicker(participants[i].id).setOptions(optionsForSelectPicker));

                if (!mandatory) {
                    var deleteButton = new YAHOO.widget.Button(participants[i].deleteButton, { disabled: mandatory, "class": "tyui" });
                    deleteButton.on("click", this.onDeleteParticipant, { deleteButton: deleteButton }, this); 
                    participantsDeleteButtons.push(deleteButton);   
                }     
            }

            this.stages[stageId] = {
                stageTime: {
                    time: inputDuoDateExpr[0],
                    timeType: selectTimeType[0]
                },
                position: stageNumber,
                element: tbodyStage[0],
                deleteButton: stageButton ? stageButton : null,
                addParticipantButton: addParticipantYUIButton,
                deleteParticpantButtons: participantsDeleteButtons,
                participants: participantsSelectPicker
            }

            if (nodeRef) {
                this.stages[stageId].nodeRef = nodeRef;
            }
        }
    });


    Citeck.widget.SelectPicker = function(htmlId) {
        Citeck.widget.SelectPicker.superclass.constructor.call(this, "Citeck.widget.SelectPicker", htmlId);
    };

    YAHOO.extend(Citeck.widget.SelectPicker, Alfresco.component.Base, {

        //  DEFAULT OPTIONS
        options: {
            context: null,

            nodeSelectCallback: null,
            nodeClickCallback: null,

            nodeSelectConstraintCallback: null,

            selectedNode: null,
            disabled: false,

            allowedAuthorityType: "USER",
            allowedGroupType: ""
        },

        restoreByDefaults: function () {
            $(this.elements.link)
                .html(this.msg("select-picker.select-person") + "<span class=\"twister\"></span>")
                .removeClass("selected");
            $(this.field).val("");

            this.elements.search.value = "";
            this.loadRootNodes(this.widgets.tree, this);

            this.closeDropDown();
        },

        onReady: function() {
            var self = this;

            this.field = Dom.get(this.id);

            this.elements = {
                box: $("<div/>", { id: this.id + "-selectPickerBox", "class": "select-picker-box" })[0],
                link: $("<a/>", { 
                                    "class": "select-picker-link", 
                                    html: this.msg("select-picker.select-person") + "<span class=\"twister\"></span>", 
                                    disabled: this.options.disabled 
                                })[0],
                dropdownList: $("<div/>", { "class": "select-picker-dropdown-list hidden", tabindex: 0 })[0],
                searchBox: $("<div/>", { "class": "select-picker-search-box" })[0],
                search: $("<input/>", { id: this.id + "-search", "class": "select-picker-search", type: "text" })[0],
                searchIcon: $("<div/>", { "class": "select-picker-search-icon", id: this.id + "-search-icon" })[0],
                tree: $("<div/>", { "class": "select-picker-tree" })[0]
            }

            var parent = this.field.parentNode,
                box = this.elements.box,
                link = this.elements.link,
                dropdownList = this.elements.dropdownList,
                searchBox = this.elements.searchBox,
                search = this.elements.search,
                searchIcon = this.elements.searchIcon,
                tree = this.elements.tree;

            // preset node
            if (this.options.selectedNode) {
                var displayName;
                if (this.options.selectedNode.displayName) {
                    displayName = this.options.selectedNode.displayName;
                } else if (this.options.selectedNode.authorityDisplayName) {
                    displayName = this.options.selectedNode.authorityDisplayName;
                } else if (this.options.selectedNode.firstName && this.options.selectedNode.lastName) {
                    displayName = this.options.selectedNode.firstName + " " + this.options.selectedNode.lastName;
                } else {
                    displayName = this.options.selectedNode.name;
                }

                $(link).html(displayName + "<span class=\"twister\"></span>");
                
                if (this.options.disabled) {
                    $(link).addClass('disabled');
                } else {
                    $(link).addClass("selected")
                }
            }

            $(searchBox)
                .append(search)
                .append(searchIcon);

            $(dropdownList)
                .append(searchBox)
                .append(tree);

            $(box)
                .append(link)
                .append(dropdownList);

            parent.replaceChild(box, this.field);
            box.appendChild(this.field);

            if (!this.options.disabled) {
                // events
                Event.addListener(link, "click", this.onClickLink, this, true);
                Event.addListener(search, "keypress", this.onSearch, this, true);

                // initialise widgets
                this.widgets = {
                    tree: new YAHOO.widget.TreeView(this.elements.tree)
                };

                this.widgets.tree.fn = {
                    loadNodeData: function(node, fnLoadComplete) {
                        var uri = self.buildTreeNodeUrl.call(self, node.data.shortName),
                            callback = {
                                success: function (oResponse) {
                                    var results = YAHOO.lang.JSON.parse(oResponse.responseText), item, treeNode;

                                    if (results) {
                                        for (var i = 0; i < results.length; i++) {
                                            item = results[i];

                                            treeNode = self.buildTreeNode(item, node, false);

                                            if (item.authorityType == "USER") {
                                                treeNode.isLeaf = true;
                                            }
                                        }
                                    }

                                    oResponse.argument.fnLoadComplete();
                                },

                                failure: function(oResponse) {
                                    // error
                                },

                                scope: self,
                                argument: {
                                  "node": node,
                                  "fnLoadComplete": fnLoadComplete
                                }
                            };
                        YAHOO.util.Connect.asyncRequest('GET', uri, callback);
                    },
                }

                // initialise treeView
                this.widgets.tree.setDynamicLoad(this.widgets.tree.fn.loadNodeData);
                self.loadRootNodes(this.widgets.tree, self);

                // Register tree-level listeners
                this.widgets.tree.subscribe("clickEvent", this.onNodeClicked, this, true);
            }
        },

        onSearch: function(event) {
            if(event.which == 13) {
                event.stopPropagation();
                
                var input = event.target,
                    query = input.value;

                if (query.length > 1) {
                    this.loadRootNodes(this.widgets.tree, this, query)
                } else if (query.length == 0) {
                    this.loadRootNodes(this.widgets.tree, this)
                }
            }
        },

        onNodeClicked: function(args) {
            var textNode = args.node,
                object = textNode.data,
                event = args.event;

            if (this.options.allowedAuthorityType.indexOf(object.authorityType) != -1) {
                if (object.authorityType == "GROUP") {
                    if (this.options.allowedGroupType && this.options.allowedGroupType.indexOf(object.groupType.toUpperCase()) == -1) {
                        return false;
                    }
                }

                if (this.options.nodeSelectConstraintCallback) {
                    if (!this.options.nodeSelectConstraintCallback(textNode, this.options.context)) { return false; }
                }

                $(this.elements.link)
                    .addClass("selected")
                    .html(object.displayName + "<span class=\"twister\"></span>");

                $(this.field).val(object.nodeRef);
                this.toggleDropDown();

                // nodeSelectCallback
                if (this.options.nodeSelectCallback) {
                    this.options.nodeSelectCallback(args, this.options.context);
                }
            }

            // nodeClickedCallback
            if (this.options.nodeClickCallback) {
                this.options.nodeClickCallback({ event: event, node: textNode });
            }
        },

        toggleDropDown: function() {
            this.elements.link.classList.toggle("open");
            this.elements.dropdownList.classList.toggle("hidden"); 
        },

        closeDropDown: function() {
            this.elements.link.classList.remove("open");
            this.elements.dropdownList.classList.add("hidden"); 
        },

        onClickLink: function() {
            this.toggleDropDown();
        },

        buildTreeNode: function(p_oItem, p_oParent, p_expanded) {
            var textNode = new YAHOO.widget.TextNode({
                    label: p_oItem.displayName || p_oItem.shortName,
                    nodeRef: p_oItem.nodeRef,
                    shortName: p_oItem.shortName,
                    displayName: p_oItem.displayName,
                    fullName: p_oItem.fullName,
                    authorityType: p_oItem.authorityType,
                    groupType: p_oItem.groupType,
                    editable: false
            }, p_oParent, p_expanded);

            if (this.options.allowedAuthorityType.indexOf(p_oItem.authorityType) != -1) {
                if (p_oItem.authorityType == "GROUP") {
                    if (!this.options.allowedGroupType || this.options.allowedGroupType.indexOf(p_oItem.groupType.toUpperCase()) != -1) {
                        textNode.className = "selectable";
                    }
                }

                if (p_oItem.authorityType == "USER") {
                    textNode.className = "selectable";
                }
            }

            return textNode;
        },

        buildTreeNodeUrl: function (group, query, excludeFields) {
            var uriTemplate ="api/orgstruct/group/"+ Alfresco.util.encodeURIPath(group) +"/children";
            if (query) {
                uriTemplate += "?branch=true&role=true&group=true&user=true&filter=" + encodeURI(query) + "&recurse=true";
            }
            if (excludeFields) {
                uriTemplate += (query ? "&" : "?") + "excludeFields=" + excludeFields;
            }
            return  Alfresco.constants.PROXY_URI + uriTemplate;
        },

        loadRootNodes: function(tree, scope, query) {
            var uri = scope.buildTreeNodeUrl("_orgstruct_home_", query),
                callback = {
                    success: function(oResponse) {
                        var results = YAHOO.lang.JSON.parse(oResponse.responseText), 
                            rootNode = tree.getRoot(), treeNode,
                            expanded = true;

                        if (results) {
                            tree.removeChildren(rootNode);

                            if (results.length > 1) expanded = false;
                            for (var i = 0; i < results.length; i++) {
                                treeNode = scope.buildTreeNode(results[i], rootNode, expanded);
                                
                                if (results[i].authorityType == "USER") {
                                    treeNode.isLeaf = true;
                                }
                            }
                        }

                        tree.draw(); 
                    },

                    failure: function(oResponse) {
                        // error
                        console.log("error")
                    },

                    scope: scope
                };

            YAHOO.util.Connect.asyncRequest('GET', uri, callback);
        }
    });


})()