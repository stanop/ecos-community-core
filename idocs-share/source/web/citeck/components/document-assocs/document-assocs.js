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
/**
 * DocumentAssocs document-details component.
 *
 * @class Citeck.widget.DocumentAssocs
 */
//if(!Citeck) Citeck = {};
//if(!Citeck.widget) Citeck.widget = {};
if (typeof Citeck == "undefined" || !Citeck) {
    var Citeck = {};
}
if (typeof Citeck.widget == "undefined" || !Citeck.widget) {
    Citeck.widget = {};
}

(function() {

    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        Element = YAHOO.util.Element;

    /**
     * DocumentAssocs constructor
     *
     * @param htmlId - identifier of component root DOM element.
     */
    Citeck.widget.DocumentAssocs = function(htmlId) {

        Citeck.widget.DocumentAssocs.superclass.constructor.call(this, "Citeck.widget.DocumentAssocs", htmlId,
            ["button", "menu", "connection", "dom", "event", "selector", "datatable", "datasource", "container" ]);

    };

    YAHOO.extend(Citeck.widget.DocumentAssocs, Alfresco.component.Base, {

        options: {

            /**
             * NodeRef of target object
             *
             * @property nodeRef
             * @type string
             */
            nodeRef: null,

            /**
             * list of cell names, that are displayed by this component
             *
             * @property cells
             * @type array
             */
            cells: [ "cm:title" ],

            /**
             * list of cell names, that will link
             *
             * @property linkCells
             * @type array
             */
            linkCells: [ "cm:title" ],

            /**
             * list of association qnames, that are displayed by this component
             *
             * @property visible
             * @type array
             */
            visible: [],

            /**
             * list of association qnames, that can be created by this component
             *
             * @property addable
             * @type array
             */
            addable: [],

            /**
             * list of association qnames, that can be removed by this component
             *
             * @property removeable
             * @type array
             */
            removeable: [],

            /**
             * dynamicTreePicker object
             *
             * @property createAssocPicker
             * @type DynamicTreePicker
             */
            createAssocPicker: null

        },

        assocsTable: [],
        assocsDataSource: [],
        types: "",
        directed: [],
        refreshed: false,
        hasPermissionWrite: false,
        selectedType: null,
        assocTypeMenu: null,

        /**
         * Event handler called when "onReady"
         *
         * @method: onReady
         */
        onReady: function() {
            var me =this;

            for (var i=0; i<this.options.visible.length; i++)
                if(this.options.visible[i].name != '') {
                    this.types == '' ? this.types = this.options.visible[i].name : this.types += ',' + this.options.visible[i].name;
                    this.directed[this.options.visible[i].name] = this.options.visible[i].directed;
                }

            this.hasPermissionWrite = false;
            var hasPermissionUrl = Alfresco.constants.PROXY_URI + 'citeck/has-permission?nodeRef=' + this.options.nodeRef+ '&permission=Write';
            YAHOO.util.Connect.asyncRequest(
                'GET',
                hasPermissionUrl, {
                    success: function (response) {
                        if (response.responseText == "true") {
                            me.hasPermissionWrite = true;
                        }
                        if(this.options.addable.length>0 && this.hasPermissionWrite) {
                            var createButton = Dom.get(this.id + '-create-button');
                            createButton.setAttribute('title', me.msg('create-button.label'));
                            createButton.setAttribute('class', 'create-link alfresco-twister-actions');

                            var onContextMenuClick = function(p_sType, p_aArgs) {
                                me.selectedType = p_aArgs[1].value;
                                me.onShowDialog();
                            };

                            me.assocTypeMenu = new YAHOO.widget.ContextMenu(
                                this.id + "-contextmenu",
                                { trigger: this.id + '-create-button' }
                            );

                            for (var i=0; i<this.options.addable.length; i++) {
                                var assoc = this.options.addable[i],
                                    type = assoc.name;
                                if(type != '') {
                                    if(assoc.direction == "both" 
                                    || assoc.direction == "target" 
                                    || assoc.direction == "undirected")
                                    {
                                        me.assocTypeMenu.addItem({
                                            'text': this.msg("association." + type.replace(":", "_") + ".target"),
                                            'value': type + ".target"
                                        });
                                    }
                                    if(assoc.direction == "both" 
                                    || assoc.direction == "source")
                                    {
                                        me.assocTypeMenu.addItem({
                                            'text': this.msg("association." + type.replace(":", "_") + ".source"),
                                            'value': type + ".source"
                                        });
                                    }
                                }
                            }

                            me.assocTypeMenu.render(this.id + '-create-button');
                            me.assocTypeMenu.clickEvent.subscribe(onContextMenuClick);
                            Dom.setStyle(this.id + '-contextmenu', "font-size", "13px");

                            Event.addListener(
                                this.id + '-create-button',
                                "click", function(event) {
                                    var xy = YAHOO.util.Event.getXY(event);
                                    me.assocTypeMenu.cfg.setProperty("xy", xy);
                                    me.assocTypeMenu.show();
                                }
                            );
                        }

                        me.options.createAssocPicker.subscribe("itemsSelected", this.onAddAssociation, this, true);

                        var searchUrl = Alfresco.constants.PROXY_URI + 'citeck/assocs?nodeRef=' + this.options.nodeRef+ '&assocTypes=' + this.types;
                        YAHOO.util.Connect.asyncRequest(
                            'GET',
                            searchUrl, {
                                success: function (response) {
                                    if (response.responseText) {
                                        var data = eval('({' + response.responseText + '})');
                                        var messageEl = Dom.get(this.id + "-message");
                                        var isAssocs = false;

                                        for (var i = 0; i < data.assocs.length; i++) {
                                            var bodyEl = Dom.get(this.id + "-body");
                                            var type = data.assocs[i].type;

                                            if(this.directed[type]) {
                                                if(data.assocs[i].sources.length != 0) {
                                                    isAssocs = true;
                                                    var table = document.createElement("div");
                                                    table.setAttribute("id", this.id + 'sources' + i + '-assocs-table');
                                                    table.setAttribute("class", "assoclist");
                                                    bodyEl.appendChild(table);
                                                    this._setupAssociationDataTable(data.assocs[i], "sources", i, type);
                                                }

                                                if(data.assocs[i].targets.length != 0) {
                                                    isAssocs = true;
                                                    var table = document.createElement("div");
                                                    table.setAttribute("id", this.id + 'targets' + i + '-assocs-table');
                                                    table.setAttribute("class", "assoclist");
                                                    bodyEl.appendChild(table);
                                                    this._setupAssociationDataTable(data.assocs[i], "targets", i, type);
                                                }
                                            } else {
                                                if(data.assocs[i].sources.length != 0 || data.assocs[i].targets.length != 0) {
                                                    isAssocs = true;
                                                    var table = document.createElement("div");
                                                    table.setAttribute("id", this.id + 'undirected' + i + '-assocs-table');
                                                    table.setAttribute("class", "assoclist");
                                                    bodyEl.appendChild(table);
                                                    data.assocs[i].undirected = data.assocs[i].sources.concat( data.assocs[i].targets);
                                                    this._setupAssociationDataTable(data.assocs[i], "undirected", i, type);
                                                }
                                            }
                                        }

                                        if(!isAssocs) {
                                            messageEl.innerHTML = me.msg("empty-assocs");
                                        }
                                    } else {
                                        var messageEl = Dom.get(this.id + "-message");
                                        messageEl.innerHTML = me.msg("assocs-load-error");
                                    }
                                },
                                failure: function() {
                                    var messageEl = Dom.get(this.id + "-message");
                                    messageEl.innerHTML = me.msg("assocs-load-error");
                                },
                                scope: this
                            }
                        );
                    },
                    scope: this
                }
            );
        },

        refreshAssociationDataTable: function QB_refreshAssociationDataTable(type, typeRef) {
            var me = this;
            var searchUrl = Alfresco.constants.PROXY_URI + 'citeck/assocs?nodeRef=' + this.options.nodeRef+ '&assocTypes=' + this.types;
            YAHOO.util.Connect.asyncRequest(
                'GET',
                searchUrl, {
                    success: function (response) {
                        if (response.responseText) {
                            var data = eval('({' + response.responseText + '})');
                            for (var i=0; i<data.assocs.length; i++) {
                                if(type == data.assocs[i].type) {
                                    var tableId = "";
                                    if(typeRef == "sources") {
                                        tableId = me.id + 'sources' + i + '-assocs-table';
                                    } else if(typeRef == "targets") {
                                        tableId = me.id + 'targets' + i + '-assocs-table';
                                    } else if(typeRef == "undirected") {
                                        tableId = me.id + 'undirected' + i + '-assocs-table';
                                        data.assocs[i].undirected = data.assocs[i].sources.concat( data.assocs[i].targets);
                                    }
                                    if(me.assocsTable[tableId] == undefined && data.assocs[i][typeRef].length > 0) {
                                        var table = document.createElement("div");
                                        table.setAttribute("id", me.id + typeRef + i + '-assocs-table');
                                        table.setAttribute("class", "assoclist");
                                        Dom.get(me.id + "-body").appendChild(table);
                                        me._setupAssociationDataTable(data.assocs[i], typeRef, i, type);
                                    } else {
                                        me.assocsTable[tableId].showTableMessage("Loading...");
                                        me.assocsTable[tableId].getDataSource().liveData = data.assocs[i];
                                        if(data.assocs[i][typeRef].length == 0) {
                                            me.assocsTable[tableId].destroy();
                                            me.assocsTable[tableId] = undefined;
                                        } else {
                                            me.assocsTable[tableId].getDataSource().sendRequest('', {
                                                success: function(sRequest, oResponse, oPayload) {
                                                    me.assocsTable[tableId].onDataReturnInitializeTable.call(me.assocsTable[tableId], sRequest, oResponse, oPayload);
                                                },
                                                failure: function(sRequest, oResponse) {
                                                    if (oResponse.status == 401) {
                                                        window.location.reload();
                                                    } else {
                                                        me.assocsTable.set("MSG_ERROR", "datasurce error");
                                                        me.assocsTable.showTableMessage("datasurce error", YAHOO.widget.DataTable.CLASS_ERROR);
                                                    }
                                                },
                                                scope: me
                                            });
                                        }
                                    }
                                }
                            }
                            var messageEl = Dom.get(this.id + "-message");
                            messageEl.innerHTML = "";
                            var isAssocs = false;
                            for (var i=0; i<data.assocs.length; i++) {
                                if(this.directed[data.assocs[i].type]) {
                                    if(data.assocs[i].sources.length != 0) {
                                        isAssocs = true;
                                    }
                                    if(data.assocs[i].targets.length != 0) {
                                        isAssocs = true;
                                    }
                                } else {
                                    if(data.assocs[i].sources.length != 0 || data.assocs[i].targets.length != 0) {
                                        isAssocs = true;
                                    }
                                }
                            }
                            if(!isAssocs) {
                                messageEl.innerHTML = me.msg("empty-assocs");
                            }
                        }
                        me.refreshed = false;
                    },
                    failure: function() {
                        if(!this.refreshed) {
                            me.refreshAssociationDataTable(type, typeRef);
                            me.refreshed = true;
                        }
                    },
                    scope: me
                }
            );
        },

        _buildCell: function QB_generateCell(attributeName, isLink) {
            return function (elCell, oRecord, oColumn, oData) {
                var openId = Alfresco.util.generateDomId(),
                    label = oRecord._oData.attributes[attributeName] || "";

                if (label) {
                    var html = '<span>' + label + '</span>',
                        page = "";

                    if (oRecord._oData.isFolder == "true") { page = "folder"; } 
                    else if (oRecord._oData.isContent == "true") { page = "document"; }

                    if (isLink) {
                        var linkTemplate = '<a id="' + openId + 
                                '" href="/share/page/' + page +  '-details?nodeRef=' + oRecord._oData.nodeRef + 
                                '" class="open-link">{cell_title}</a>';

                        html = linkTemplate.replace("{cell_title}", html);
                    }

                    elCell.innerHTML = html;
                }
            }
        },

        _setupAssociationDataTable: function QB_setupAssociationDataTable(dataJSON, resultsListName, index, type) {
            var me = this,
                columnDefinitions = [],
                object = dataJSON[resultsListName][index];

            // render cells
            for (var c in me.options.cells) {
                var cellName = me.options.cells[c],
                    isLink = me.options.linkCells.indexOf(cellName) != -1,
                    label = object.properties[cellName] ? object.properties[cellName].label : "";

                if (cellName) {
                    columnDefinitions.push({
                        key: cellName.replace(/\w+:/, ""),
                        label: label,
                        sortable: false, 
                        formatter: me._buildCell(cellName, isLink)
                    });
                }
            }

            // render cell actions
            columnDefinitions.push({
                key: "actions", sortable: false, label: "",
                formatter: function (elCell, oRecord, oColumn, oData) {
                    Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
                    var isRemoveableType = false;
                    for (var i=0; i<me.options.removeable.length; i++)
                        if(me.options.removeable[i].name == type) {
                            isRemoveableType = true;
                            break;
                        }
                    if(isRemoveableType && me.hasPermissionWrite) {
                        Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
                        var removeId = Alfresco.util.generateDomId();
                        elCell.innerHTML = '<div class="remove-assoc"><a title=' + me.msg("delete-button.label") + ' id="' + removeId + '" class="remove-link">' +
                            '<span>&nbsp</span></a></div>';
                        Event.addListener(
                            removeId,
                            "click", function() {
                                me.onRemoveAssociation.apply(me, ['', oRecord, type]);
                            }
                        );
                    }
                }
            });

            var tableId = this.id + resultsListName + index + '-assocs-table';
            this.assocsDataSource[tableId] = new YAHOO.util.DataSource(dataJSON, {
                responseType: YAHOO.util.DataSource.TYPE_JSON,
                responseSchema: {
                    resultsList : resultsListName, // String pointer to result data
                    // Field order doesn't matter and not all data is required to have a field
                    fields : [
                        { key: "attributes" },
                        { key: "nodeRef" },
                        { key: "name" },
                        { key: "isFolder" },
                        { key: "isContent" },
                        { key: "typeDirect" },
                        { key: "title" }
                    ]
                }
            });

            var caption = resultsListName == "undirected" ? "targets" : resultsListName,
                myConfigs = {
                    caption: this.msg("association." + type.replace(":", "_") + "." + caption.slice(0,-1))
                },
                accocTable = new YAHOO.widget.DataTable(
                    this.id + resultsListName + index + '-assocs-table',
                    columnDefinitions,
                    this.assocsDataSource[tableId],
                    myConfigs
                )

            this.assocsTable[tableId] = accocTable;
        },

        onRemoveAssociation: function QB_onRemoveTargetAssociation(event, obj, type) {
            var me = this;
            var nodeRef = this.options.nodeRef;
            Alfresco.util.PopupManager.displayPrompt(
                {
                    title: me.msg("delete-button.label"),
                    text: me.msg("delete-confirmation") + " '" + obj._oData.name + "' ?",
                    nodeRef : this.options.nodeRef,
                    noEscape: true,
                    buttons: [
                        {
                            text: me.msg("button.delete"),
                            handler: function dlA_onActionDelete_delete()
                            {
                                this.destroy();
                                var deleteUrl = "";
                                if(obj._oData.typeDirect == "sources") {
                                    deleteUrl = Alfresco.constants.PROXY_URI + 'citeck/remove-assocs?sourceRef=' + obj['_oData']['nodeRef'] + '&targetRef=' + me.options.nodeRef +'&assocTypes=' + type;
                                } else {
                                    deleteUrl = Alfresco.constants.PROXY_URI + 'citeck/remove-assocs?sourceRef=' + me.options.nodeRef + '&targetRef=' + obj['_oData']['nodeRef'] +'&assocTypes=' + type;
                                }
                                if(!me.directed[type]) {
                                    obj._oData.typeDirect = 'undirected';
                                }
                                YAHOO.util.Connect.asyncRequest(
                                    'DELETE',
                                    deleteUrl, {
                                        success: function (response) {
                                            if (response.responseText) {
                                                this.refreshAssociationDataTable(type, obj._oData.typeDirect);
                                            }
                                        },
                                        failure: function() {
                                            Alfresco.logger.error("deleting association error");
                                        },
                                        scope: me
                                    });
                            }
                        },
                        {
                            text: me.msg("button.cancel"),
                            handler: function dlA_onActionDelete_cancel()
                            {
                                this.destroy();
                            },
                            isDefault: true
                        }]
                });
        },

        onShowDialog: function QB_onShowDialog() {
            this.options.createAssocPicker.setSelectedItems([]);
            this.options.createAssocPicker.show();
        },

        onAddAssociation: function QB_onAddAssociation(obj) {
            var me = this;
            var errorMsg = "";
            var error = false;
            if(obj.length == 0) {
                errorMsg = me.msg("error.not-selected-elements");
                error = true;
            } else {
                for (var i = 0; i < obj.length; i++) {
                    if(obj[i].nodeRef == me.options.nodeRef) {
                        errorMsg = me.msg("error.assoc-incorrect");
                        error = true;
                    }
                }
            }
            if(error) {
                me.showErrorDialog(me.msg("validation-error"), errorMsg);
            } else {
                var menuItem = me.selectedType.split('.');
                var assocType = menuItem[0];
                var assocRefType = menuItem[1];
                var addUrl = "";
                var documentsNodeRefs = "";
                for (var j = 0; j < obj.length; j++) {
                    documentsNodeRefs += documentsNodeRefs == "" ? obj[j].nodeRef : "," + obj[j].nodeRef;
                }

                if(assocRefType == "source") {
                    addUrl = Alfresco.constants.PROXY_URI + 'citeck/add-assocs?sourceRef=' + documentsNodeRefs + '&targetRef=' + me.options.nodeRef +'&assocTypes=' + assocType;
                } else {
                    addUrl = Alfresco.constants.PROXY_URI + 'citeck/add-assocs?sourceRef=' + me.options.nodeRef + '&targetRef=' + documentsNodeRefs +'&assocTypes=' + assocType;
                }
                if(!me.directed[assocType]) {
                    assocRefType = "undirected";
                } else {
                    assocRefType += "s";
                }
                YAHOO.util.Connect.asyncRequest(
                    'POST',
                    addUrl, {
                        success: function (response) {
                            if (response.responseText) {
                                var result = eval('({' + response.responseText + '})');
                                if(result.data == false) {
                                    me.showErrorDialog(me.msg("adding-error"), me.msg("adding-error.already-exist"));
                                }
                                me.refreshAssociationDataTable(assocType, assocRefType);
                            }
                        },
                        failure: function(response) {
                            me.showErrorDialog(me.msg("adding-error"), me.msg("adding-error.can-not-be-linked"));
                            me.refreshAssociationDataTable(assocType, assocRefType);
                        },
                        scope: me
                    }
                );
            }
        },

        showErrorDialog: function QB_showErrorDialog(title, text) {
            Alfresco.util.PopupManager.displayPrompt(
                {
                    title: title,
                    text: text,
                    noEscape: true,
                    buttons: [
                        {
                            text: this.msg("button.ok"),
                            handler: function dlA_onAction_cancel()
                            {
                                this.destroy();
                            }
                        }]
                });
        }

    });

})();
