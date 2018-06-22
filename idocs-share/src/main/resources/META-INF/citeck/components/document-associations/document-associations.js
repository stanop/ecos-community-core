/*
 * Copyright (C) 2008-2017 Citeck LLC.
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
 * DocumentAssociations document-details component.
 *
 * @class Citeck.widget.DocumentAssociations
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
     * DocumentAssociations constructor
     *
     * @param htmlId - identifier of component root DOM element.
     */
    Citeck.widget.DocumentAssociations = function(htmlId) {

        Citeck.widget.DocumentAssociations.superclass.constructor.call(this, "Citeck.widget.DocumentAssociations", htmlId,
            ["button", "menu", "connection", "dom", "event", "selector", "datatable", "datasource", "container" ]);

    };

    YAHOO.extend(Citeck.widget.DocumentAssociations, Alfresco.component.Base, {

        options: {

            /**
             * NodeRef of target object
             *
             * @property nodeRef
             * @type string
             */
            nodeRef: null,

            /**
             * list of column objects, that are displayed by this component
             *
             * @property columns
             * @type array
             */
            columns: [ { attribute: "cm:title", isLink: true } ],


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
             * multiple select
             *
             * @property multiple
             * @type Boolean
             */
            isMultiple: true
        },

        assocsTable: [],
        assocsDataSource: [],
        types: "",
        directed: [],
        refreshed: false,
        hasPermissionWrite: false,
        selectedType: null,
        assocTypeMenu: null,
        addedAssocsDocuments: {},
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

                            me.assocTypeMenu = new YAHOO.widget.ContextMenu(
                                this.id + "-contextmenu",
                                { trigger: this.id + '-create-button'}
                            );

                            var controlId = me.id + "-control";

                            require(['citeck/components/journals2/journals', 'citeck/utils/knockout.utils', 'citeck/utils/knockout.yui', 'citeck/utils/knockout.components',
                                'citeck/utils/knockout.invariants-controls', 'lib/knockout'], function(journals, koutils, koyui, kocomponents, koic, ko) {
                                var Node = koutils.koclass('invariants.Node'),
                                    selectedValue = ko.observable(),
                                    journalType = ko.observable(),
                                    nodeType = ko.observable(),
                                    protected = ko.observable(false),
                                    isMultiple = ko.observable(me.options.isMultiple);

                                selectedValue.subscribe(function(newValue) {
                                    if (newValue) {
                                        var isArray = _.isArray(newValue);
                                        this.onAddAssociation(isArray ? newValue : [newValue]);
                                    }

                                }, me);
                                ko.applyBindings({
                                    "fieldId": controlId + "-field",
                                    "name": ko.observable(controlId + "-create-field"),
                                    "multiple": isMultiple,
                                    "value": selectedValue,
                                    "protected": protected,
                                    "journalId": journalType,
                                    "nodetype": nodeType,
                                    "filterOptions":function(criteria, pagination) {

                                        if (!this.cache) this.cache = {};
                                        if (!this.cache.result) {
                                            this.cache.result = ko.observable([]);
                                            this.cache.result.extend({notify: 'always'});
                                        }

                                        var query = {
                                            skipCount: 0,
                                            maxItems: 10
                                        };
                                        if (!_.find(criteria, function (criterion) {
                                                return criterion.predicate == 'journal-id';
                                            })) {
                                            if (!this.nodetype()) {
                                                return [];
                                            }

                                            query['field_1'] = "type";
                                            query['predicate_1'] = "type-equals";
                                            query['value_1'] = this.nodetype();
                                        }

                                        if (pagination) {
                                            if (pagination.maxItems) query.maxItems = pagination.maxItems;
                                            if (pagination.skipCount) query.skipCount = pagination.skipCount;
                                        }

                                        _.each(criteria, function(criterion, index) {
                                            query['field_' + (index + 2)] = criterion.attribute;
                                            query['predicate_' + (index + 2)] = criterion.predicate;
                                            query['value_' + (index + 2)] = criterion.value;
                                        });

                                        if(this.cache.query) {
                                            if(_.isEqual(query, this.cache.query)) return this.cache.result();
                                        }

                                        this.cache.query = query;
                                        if (_.some(_.keys(query), function(p) {
                                                return _.some(["field", "predicate", "value"], function(ci) {
                                                    return p.indexOf(ci) != -1;
                                                });
                                            })) {
                                            Alfresco.util.Ajax.jsonPost({
                                                url: Alfresco.constants.PROXY_URI + "search/criteria-search",
                                                dataObj: query,
                                                successCallback: {
                                                    scope: this.cache,
                                                    fn: function(response) {
                                                        var result = _.map(response.json.results, function(node) {
                                                            return new Node(node);
                                                        });
                                                        result.pagination = response.json.paging;
                                                        result.query = response.json.query;
                                                        this.result(result);
                                                    }
                                                }
                                            });
                                        }

                                        return this.cache.result();
                                    }
                                }, document.getElementById(controlId + "-container"));

                                var addAssociation = function (journalId, journalControlId, selectedType) {
                                    journalType(journalId);
                                    me.selectedType = selectedType;

                                    $("#" + controlId + "-button").click();
                                    Event.on(controlId + "-journalPanel-submitInput", "click", function (event) {
                                        if (isMultiple) {
                                            selectedValue(null);
                                        }
                                    });
                                };

                                var getItemsUrl = Alfresco.constants.PROXY_URI + 'citeck/cardlets/sites-and-journals';

                                YAHOO.util.Connect.asyncRequest(
                                    'GET',
                                    getItemsUrl, {
                                        success: function (response) {
                                            if (response.responseText) {
                                                var data = eval('(' + response.responseText + ')');
                                                if (data && data.sites && data.sites.length) {
                                                    var getSubmenu = function (type) {
                                                        var submenu = {id: type.replace(":", "_")};
                                                        submenu.itemdata = data.sites.map(function(item) {
                                                            var siteId = item.siteId + "-" + type.replace(":", "-");
                                                            return {
                                                                text: item.siteName,
                                                                submenu: {
                                                                    id: siteId,
                                                                    itemdata: item.journals.map(function(journal) {
                                                                        return {
                                                                            id: controlId + "-" + siteId + "-" + journal.journalId,
                                                                            text: journal.journalName,
                                                                            onclick: { fn: addAssociation.bind(null, journal.journalId, journal.journalType, type)}
                                                                        }
                                                                    })
                                                                }
                                                            }
                                                        });
                                                        return submenu;
                                                    };
                                                    for (var  j = 0; j < me.options.addable.length; j++) {
                                                        var assoc = me.options.addable[j],
                                                            type = assoc.name;
                                                        if(type != '') {
                                                            if(assoc.direction == "both"
                                                                || assoc.direction == "target"
                                                                || assoc.direction == "undirected") {
                                                                me.assocTypeMenu.addItem({
                                                                    text: me.msg("association." + type.replace(":", "_") + ".target"),
                                                                    submenu : getSubmenu(type)
                                                                });
                                                            }
                                                            if(assoc.direction == "both" || assoc.direction == "source") {
                                                                me.assocTypeMenu.addItem({
                                                                    text: me.msg("association." + type.replace(":", "_") + ".source"),
                                                                    submenu : getSubmenu(type)
                                                                });
                                                            }
                                                        }
                                                    }

                                                    me.assocTypeMenu.render(me.id + '-create-button');
                                                }
                                            }
                                        },
                                        failure: function() {
                                            var messageEl = Dom.get(this.id + "-message");
                                            messageEl.innerHTML = me.msg("assocs-load-error");
                                        },
                                        scope: this
                                    }
                                );
                            });

                            Dom.setStyle(me.id + '-contextmenu', "font-size", "13px");

                            Event.addListener(
                                this.id + '-create-button',
                                "click", function(event) {
                                    var xy = YAHOO.util.Event.getXY(event);
                                    me.assocTypeMenu.cfg.setProperty("xy", xy);
                                    me.assocTypeMenu.show();
                                }
                            );
                        }

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

                                            me.addedAssocsDocuments[type] = data.assocs[i].sources.concat(data.assocs[i].targets);

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
                                    me.addedAssocsDocuments[type] = data.assocs[i].sources.concat(data.assocs[i].targets);
                                    var tableId = "";
                                    if(typeRef == "sources") {
                                        tableId = me.id + 'sources' + i + '-assocs-table';
                                    } else if(typeRef == "targets") {
                                        tableId = me.id + 'targets' + i + '-assocs-table';
                                    } else if(typeRef == "undirected") {
                                        tableId = me.id + 'undirected' + i + '-assocs-table';
                                        data.assocs[i].undirected = data.assocs[i].sources.concat( data.assocs[i].targets);
                                    }
                                    if(me.assocsTable[tableId] == undefined) {
                                        var table = document.createElement("div");
                                        table.setAttribute("id", me.id + typeRef + i + '-assocs-table');
                                        table.setAttribute("class", "assoclist");
                                        Dom.get(me.id + "-body").appendChild(table);
                                        if (data.assocs[i][typeRef] && data.assocs[i][typeRef].length) {
                                            me._setupAssociationDataTable(data.assocs[i], typeRef, i, type);
                                        }
                                    } else {
                                        me.assocsTable[tableId].showTableMessage("Loading...");
                                        me.assocsTable[tableId].getDataSource().liveData = data.assocs[i];
                                        if(!data.assocs[i][typeRef] || (data.assocs[i][typeRef] && data.assocs[i][typeRef].length == 0)) {
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
                                    if(data.assocs[i].sources.length || data.assocs[i].targets.length) {
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

        _buildCell: function QB_buildCell(attributeName, isLink) {
            var me = this,
                column = me.options.columns[_.findIndex(this.options.columns, { attribute: attributeName })];

            return function(elCell, oRecord, oColumn, oData) {
                if (!oRecord._oData.attributes[attributeName]) {
                    elCell.innerHTML = '<span>-</span>';
                    return;
                }

                if (column.formatter) {
                    column.formatter(elCell, oRecord, oColumn, oRecord._oData.attributes[attributeName]);
                    return;
                }

                var openId = Alfresco.util.generateDomId(),
                    label = oRecord._oData.attributes[attributeName];
                html = '<span>' + label + '</span>',
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
        },

        _buildColumnDefinitions: function QB_buildColumnDefinitions(type) {
            var me = this,
                columnDefinitions = [];

            // render columns
            for (var c = 0; c < me.options.columns.length; c++) {
                var column = me.options.columns[c];

                columnDefinitions.push({
                    key: column.attribute.replace(/\w+:/, ""),
                    label: column.label,
                    sortable: false,
                    formatter: me._buildCell(column.attribute, column.isLink)
                });
            }

            // render column actions
            columnDefinitions.push({
                key: "actions", sortable: false, label: "",
                formatter: function (elCell, oRecord, oColumn, oData) {
                    Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

                    var isRemoveableType = false;
                    for (var i = 0; i < me.options.removeable.length; i++) {
                        if (me.options.removeable[i].name == type) {
                            isRemoveableType = true;
                            break;
                        }
                    }

                    if (isRemoveableType && me.hasPermissionWrite) {
                        Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

                        var removeId = Alfresco.util.generateDomId();
                        elCell.innerHTML = '<div class="remove-assoc"><a title=' + me.msg("delete-button.label") + ' id="' + removeId + '" class="remove-link">' +
                            '<span>&nbsp</span></a></div>';

                        Event.addListener( removeId, "click", function() {
                            me.onRemoveAssociation.apply(me, ['', oRecord, type]);
                        });
                    }
                }
            });

            return columnDefinitions;
        },

        _buildDataTable: function QB_buildDataTable(dataJSON, resultsListName, index, type) {
            var tableId = this.id + resultsListName + index + '-assocs-table';

            this.assocsDataSource[tableId] = new YAHOO.util.DataSource(dataJSON, {
                responseType: YAHOO.util.DataSource.TYPE_JSON,
                responseSchema: {
                    resultsList : resultsListName, // String pointer to result data
                    // Field order doesn't matter and not all data is required to have a field
                    fields : [
                        { key: "attributes" },
                        { key: "nodeRef" },
                        { key: "isFolder" },
                        { key: "isContent" },
                        { key: "typeDirect" },
                    ]
                }
            });

            var caption = resultsListName == "undirected" ? "targets" : resultsListName,
                myConfigs = {
                    caption: this.msg("association." + type.replace(":", "_") + "." + caption.slice(0,-1))
                },
                accocTable = new YAHOO.widget.DataTable(
                    this.id + resultsListName + index + '-assocs-table',
                    this._buildColumnDefinitions(type),
                    this.assocsDataSource[tableId],
                    myConfigs
                );

            this.assocsTable[tableId] = accocTable;
        },

        _setupAssociationDataTable: function QB_setupAssociationDataTable(dataJSON, resultsListName, index, type) {
            var me = this;

            // get labels for attributes without label property
            var noLabelAttributes = [];
            for (var c = 0; c < me.options.columns.length; c++) {
                if (!me.options.columns[c].label) noLabelAttributes.push(me.options.columns[c].attribute);
            }

            if (noLabelAttributes.length > 0) {
                var getTitlesURL = Alfresco.constants.PROXY_URI + "citeck/util/attributes/title?attributes=" + noLabelAttributes.join(",");

                YAHOO.util.Connect.asyncRequest('GET', getTitlesURL, {
                    success: function (response) {
                        if (response.responseText) {
                            var result = eval("(" + response.responseText + ")");
                            for (var c = 0; c < this.options.columns.length; c++) {
                                if (!this.options.columns[c].label) {
                                    this.options.columns[c].label = result[this.options.columns[c].attribute];
                                }
                            }
                            this._buildDataTable(dataJSON, resultsListName, index, type);
                        }
                    },
                    scope: me
                });
            } else { this._buildDataTable(dataJSON, resultsListName, index, type); }
        },

        onRemoveAssociation: function QB_onRemoveTargetAssociation(event, obj, type) {
            var me = this;
            var nodeRef = this.options.nodeRef;
            Alfresco.util.PopupManager.displayPrompt(
                {
                    title: me.msg("delete-button.label"),
                    text: me.msg("delete-confirmation") + " '" + (obj._oData.attributes["cm:title"] || "no-title") + "' ?",
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

        onAddAssociation: function QB_onAddAssociation(obj) {
            var me = this;
            var errorMsg = "";
            if(obj.length == 0) {
                me.showErrorDialog(me.msg("validation-error"), me.msg("error.not-selected-elements"));
            } else {
                for (var i = 0; i < obj.length; i++) {
                    if (obj[i].nodeRef == me.options.nodeRef) {
                        errorMsg = me.msg("error.assoc-incorrect") + ": " + obj[i].properties["cm:title"];
                    }
                }

                if (!errorMsg || (me.options.isMultiple && obj.length > 1)) {
                    var menuItem = me.selectedType.split('.');
                    var assocType = menuItem[0];
                    var assocRefType = menuItem[1];
                    var addUrl = "";
                    var documentsNodeRefs = "";
                    var reAddingDocument = "";
                    var allErrors = "";
                    for (var j = 0; j < obj.length; j++) {
                        var currentNodeRef = obj[j].nodeRef,
                            isAdded = me.addedAssocsDocuments[assocType].find(function (doc) {
                                return doc.nodeRef == currentNodeRef
                            });
                        if (!isAdded && currentNodeRef != me.options.nodeRef) {
                            documentsNodeRefs += documentsNodeRefs == "" ? currentNodeRef : "," + currentNodeRef;
                        }
                        if (isAdded) {
                            reAddingDocument += "<br>" + (obj[j].properties["cm:title"] || "no-title");
                        }
                    }

                    var allErrors = (errorMsg ? (errorMsg + "<br>") : "") +
                                    (reAddingDocument ? me.msg("adding-error.re-adding") + reAddingDocument : "");

                    if (documentsNodeRefs) {
                        if (assocRefType == "source") {
                            addUrl = Alfresco.constants.PROXY_URI + 'citeck/add-assocs?sourceRef=' + documentsNodeRefs + '&targetRef=' + me.options.nodeRef + '&assocTypes=' + assocType;
                        } else {
                            addUrl = Alfresco.constants.PROXY_URI + 'citeck/add-assocs?sourceRef=' + me.options.nodeRef + '&targetRef=' + documentsNodeRefs + '&assocTypes=' + assocType;
                        }
                        if (!me.directed[assocType]) {
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
                                        if (result.data == false) {
                                            me.showErrorDialog(me.msg("adding-error"), me.msg("adding-error.already-exist") + (allErrors ? "<br>" + allErrors : ""));
                                        }
                                        me.refreshAssociationDataTable(assocType, assocRefType);
                                    }
                                },
                                failure: function (response) {
                                    me.showErrorDialog(me.msg("adding-error"), me.msg("adding-error.can-not-be-linked") + (allErrors ? "<br>" + allErrors : ""));
                                    me.refreshAssociationDataTable(assocType, assocRefType);
                                },
                                scope: me
                            }
                        );
                    } else if (allErrors) {
                        me.showErrorDialog(me.msg("adding-error"), allErrors);
                    }
                } else if (errorMsg) {
                    me.showErrorDialog(me.msg("validation-error"), errorMsg);
                }
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
