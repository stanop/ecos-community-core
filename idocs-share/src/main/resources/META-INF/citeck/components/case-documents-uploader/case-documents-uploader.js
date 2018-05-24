/**
 * CaseDocumentsUploader  component.
 *
 * @class Citeck.widget.CaseDocumentsUploader
 */
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
     * FamiliarStory constructor
     *
     * @param htmlId - identifier of component root DOM element.
     */
    Citeck.widget.CaseDocumentsUploader = function(htmlId) {

        Citeck.widget.CaseDocumentsUploader.superclass.constructor.call(this, "Citeck.widget.CaseDocumentsUploader", htmlId,
            ["dom", "button", "menu", "selector", "connection", "datasource", "datatable", "event", "container"]);
        
        this.recordsLoader = new Citeck.utils.DoclibRecordLoader('injournal');
        
        this.widgets.doclist = new Citeck.widget.HandyDocumentList(this.id + "-doclist").setOptions({
            rootNode: "alfresco://company/home",
            usePagination: true,
            oldSchoolActions: true,
            viewRendererName: 'gallery',
            viewRenderers: [
                { id: "simple", label: 'simple', iconClass: 'simple', widget: "Alfresco.DocumentListSimpleViewRenderer" },
                { id: "filmstrip", label: "filmstrip", iconClass: "filmstrip", widget: "Alfresco.DocumentListFilmstripViewRenderer"},
                { id: "gallery", label: "gallery", iconClass: "gallery", widget: "Alfresco.DocumentListGalleryViewRenderer"}
            ]
        });

        YAHOO.Bubbling.on("handyDoclistReady", function() {
            var fileSelect = this.widgets.doclist.widgets.fileSelect,
                fileSelectMenu = fileSelect.getMenu();

            fileSelectMenu.beforeRenderEvent.subscribe(function() {
                // remove 'selectDocuments' and 'selectFolders'
                this.removeItem(this.getItem(0));
                this.removeItem(this.getItem(0));

                // destroy listener
                this.beforeRenderEvent.unsubscribe();
            });

            var selectedItemsContainer = $("<div>", { "class": "selected-items" });
            selectedItemsContainer.insertAfter($(".file-select", Dom.get(htmlId + "-doclist-doclistBar").children[0]));

            var selectedItemsMenuButton = new YAHOO.widget.Button({ 
                type: "menu", 
                label: this.msg("button.selected-items"),
                id: htmlId + "-selected-items-menu-button",
                menu: [
                    { 
                        text: this.msg("button.delete"), 
                        onclick: {
                            fn: function(type, args, item) {
                                var self = this;

                                var files = this.widgets.doclist.widgets.dataSource.liveData.items,
                                    selectedFiles = this.widgets.doclist.selectedFiles,
                                    selectedFilesNodeRefs = [];

                                for (var key in selectedFiles) {
                                    if (selectedFiles[key]) selectedFilesNodeRefs.push(key);
                                }

                                if (selectedFilesNodeRefs.length > 0) {
                                    Alfresco.util.PopupManager.displayPrompt({
                                        title: this.msg("button.delete"),
                                        text: this.msg("dialog.delete-confirmation-all-items"),
                                        noEscape: true,
                                        buttons: [
                                            {
                                                text: this.msg("button.delete"),
                                                handler: function() {
                                                    this.hide();

                                                    self.waitMessage = Alfresco.util.PopupManager.displayMessage({
                                                        text: self.msg("message.multiple-delete.please-wait"),
                                                        displayTime: 0,
                                                        modal: true
                                                    });

                                                    YAHOO.util.Connect.asyncRequest("POST",
                                                        Alfresco.constants.PROXY_URI + "slingshot/doclib/action/files?alf_method=delete", 
                                                        {
                                                            success: function (response) {
                                                                // destroy wait message
                                                                self.waitMessage.destroy();

                                                                var deletedFilesNodeRefs = selectedFilesNodeRefs;

                                                                var result = JSON.parse(response.responseText);
                                                                if (result.failureCount == 0) {
                                                                    // show success message
                                                                    self.message = Alfresco.util.PopupManager.displayMessage({
                                                                        text: self.msg("message.files-delete-success")
                                                                    })
                                                                } else {    
                                                                    deletedFilesNodeRefs = _.map(result.results, function(file) {
                                                                        if (file.success) return file.nodeRef;
                                                                    });

                                                                    // show half-success message
                                                                    self.message = Alfresco.util.PopupManager.displayMessage({
                                                                        text: self.msg("message.files-delete-half-success")
                                                                    })
                                                                }

                                                                // update doclist
                                                                var filesAfterDeleting = _.difference(_.pluck(files, 'nodeRef'), deletedFilesNodeRefs);
                                                                self.recordsLoader.load(filesAfterDeleting, function(nodeRefs, results) {
                                                                    self.widgets.doclist.loadData(results);
                                                                });

                                                                // clear selectedFiles in doclist
                                                                self.widgets.doclist.selectedFiles = {};

                                                                // destroy success message
                                                                setTimeout(function() {
                                                                    if (self.message) self.message.destroy();
                                                                }, 2000)
                                                                
                                                                // fire event about file deleted
                                                                YAHOO.Bubbling.fire("fileDeleted");
                                                            },
                                                            failure: function() {
                                                                // destroy wait message
                                                                self.waitMessage.destroy();

                                                                // show failure message
                                                                self.message = Alfresco.util.PopupManager.displayMessage({
                                                                    text: self.msg("message.files-delete-failure")
                                                                })

                                                                // destroy success message
                                                                setTimeout(function() {
                                                                    if (self.message) self.message.destroy();
                                                                }, 2000)
                                                            },

                                                            scope: this
                                                        },
                                                        JSON.stringify({ nodeRefs: selectedFilesNodeRefs })
                                                    );

                                                    this.destroy();
                                                }
                                            },
                                            {
                                                text: this.msg("button.cancel"),
                                                handler: function() {
                                                    this.destroy();
                                                },
                                                isDefault: true
                                            }
                                        ]
                                    });
                                } else {
                                    Alfresco.util.PopupManager.displayMessage({
                                        text: self.msg("dialog.delete-nothing")
                                    });
                                }
                                
                            },
                            scope: this
                        }
                    }
                ], 
                container: selectedItemsContainer[0]
            });

            // set right zindex for selected items menu
            selectedItemsMenuButton.getMenu().cfg.setProperty("zindex", 2);
        }, this)

        _.each(['metadataRefresh', 'fileDeleted'], function(event) {
            YAHOO.Bubbling.on(event, this.refreshDataTable, this);
        }, this)

        YAHOO.Bubbling.on('fileDeletedFromDoclibAction', this._refreshDoclibData, this);
    };

    YAHOO.extend(Citeck.widget.CaseDocumentsUploader, Alfresco.component.Base, {

        options: {
            nodeRef: null,

            intermediateDialog: {
                formId: null
            }

        },

        documentKindsTable: null,
        documentKindsDataSource: null,
        uploadDialog: null,
        caseButtonMenu : null,
        typeButtonMenu : null,
        kindButtonMenu : null,
        caseFilterValue : "all",
        caseFilterIndex : 0,
        typeFilterValue : "all",
        typeFilterIndex : 0,
        caseUploadNodeRef : null,
        dndRequest: null,
        data: null,
        intermediateFileDialog: null,

        /**
         * Event handler called when "onReady"
         *
         * @method: onReady
         */
        onReady: function() {
            var me = this;
            var getCaseDocumentsUrl = Alfresco.constants.PROXY_URI + 'citeck/cases/case-documents?nodeRef=' + this.options.nodeRef;
            
            YAHOO.util.Connect.asyncRequest(
                'GET',
                getCaseDocumentsUrl, {
                    success: function (response) {
                        if (response.responseText) {

                            var data = me.data = Alfresco.util.parseJSON(response.responseText);

                            me.addHeaderButtons();
                            me.addUploadDialog();
                            me.addViewDialog();
                            me.addIntermediateFileDialog();

                            var tableResult = { rowList : new Array()};
                            tableResult.rowList = me.searchDocumentKinds(data.container, tableResult.rowList,
                                data.containerKinds, data.documentTypes, data.documentKinds);
                            tableResult.rowList.sort(me.sortTable);

                            var renderCellStatus = function CDU_renderCellStatus(elCell, oRecord, oColumn, oData) {
                                var status = oRecord._oData.status;
                                if(status == "uploaded") {
                                    elCell.innerHTML = '<div class="uploaded-status" title="' + me.msg('status.uploaded') + '"></div>';
                                } else if(status == "notUploadedMandatory") {
                                    elCell.innerHTML = '<div class="mandatory-status" title="' + me.msg('status.mandatory-not-uploaded') + '"></div>';
                                } else{
                                    elCell.innerHTML = '<div class="not-mandatory-status" title="' + me.msg('status.optional-not-uploaded') + '"></div>';
                                }
                            };

                            var renderCellTypeFile = function CDU_renderCellTypeFile(elCell, oRecord, oColumn, oData) {
                                var files = oRecord.getData("files"),
                                    html;
                                switch(files.length) {
                                case 0: 
                                    html = ''; 
                                    break;
                                case 1: 
                                    var fileName = files[0].name; 
                                    html = '<span title="' + fileName + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 
                                        'components/images/filetypes/' + Alfresco.util.getFileIcon(fileName, null, 16) + '" /></span>';
                                    break;
                                default:
                                    html = '<span title="' + me.msg("multiple-files.title", files.length) + '"><img src="' + 
                                        Alfresco.constants.URL_RESCONTEXT + 'components/images/multiple-files-16.png" /></span>';
                                }
                                elCell.innerHTML = html;
                            };
                            
                            var countFileLink = 0;

                            var renderCellFileLink = function CDU_renderCellStatus(elCell, oRecord, oColumn, oData) {
                                var caseName = oRecord.getData("caseName"),
                                    kindRef = oRecord.getData("kindNodeRef"),
                                    kindName = oRecord.getData("kindName"),
                                    files = oRecord.getData("files"),
                                    html,
                                    linkId = oRecord.getId() + "-preview-file-link";

                                switch(files.length) {
                                    case 0: 
                                        html = kindName; 
                                        break;
                                    case 1:
                                        html = '<span title="' + files[0].name + '"><a id="' + linkId + '">' + kindName + '</a></span>';

                                        YAHOO.util.Event.on(linkId, 'click', function() {
                                            if (!me.intermediateFileDialog || me.intermediateFileDialog.options.file != files[0]) {
                                                me.intermediateFileDialog = new Citeck.widget.intermediateFileDialog(me.widgets.intermediateFileDialog.id).setOptions({
                                                    propertiesFormId: me.options.intermediateDialog.formId,
                                                    panel: me.widgets.intermediateFileDialog,
                                                    file: files[0]
                                                }).setMessages(Alfresco.messages.scope["Citeck.widget.CaseDocumentsUploader"]);
                                            }

                                            me.widgets.intermediateFileDialog.setHeader(caseName + ": " + kindName);
                                            me.widgets.intermediateFileDialog.show();
                                        });
                                        break;
                                    default:
                                        countFileLink++;
                                        html = '<span title="' + me.msg("multiple-files.title", files.length) + '">' 
                                            + '<a id="file-link-' + kindRef + "-" + countFileLink + '">' + kindName + '</a></span>';
                                        YAHOO.util.Event.on('file-link-' + kindRef + "-" + countFileLink, 'click', function() {
                                            me.recordsLoader.load(_.pluck(files, 'nodeRef'), function(nodeRefs, results) {
                                                me.widgets.doclist.loadData(results);
                                            });
                                            me.widgets.viewDialog.setHeader(caseName + ": " + kindName);
                                            me.widgets.viewDialog.show();
                                        });
                                }
                                elCell.innerHTML = html;
                            };

                            if(tableResult.rowList.length !=0 ){
                                var columnDefinitions =
                                    [
                                        { key: "status", label: me.msg("table.header.status"), sortable:true, formatter: renderCellStatus},
                                        { key: "typeFile", label: "", formatter: renderCellTypeFile},
                                        { key: "kindName", label: me.msg("table.header.name"), sortable:true, formatter: renderCellFileLink},
                                        { key: "typeName", label: me.msg("table.header.type"), sortable:true},
                                        { key: "caseName", label: me.msg("table.header.caseName"), sortable:true},
                                        { key: "uploader", label: me.msg("table.header.uploader"), sortable:true},
                                        { key: "uploaded", label: me.msg("table.header.uploaded"), sortable:true}
                                    ];

                                var tableId = this.id + '-case-documents-uploader-table';
                                this.documentKindsDataSource = new YAHOO.util.DataSource(tableResult, {
                                    responseType: YAHOO.util.DataSource.TYPE_JSON,
                                    responseSchema: {
                                        resultsList : "rowList", // String pointer to result data
                                        // Field order doesn't matter and not all data is required to have a field
                                        fields : [
                                            { key: "status" },
                                            { key: "kindName" },
                                            { key: "kindNodeRef" },
                                            { key: "kindMandatory" },
                                            { key: "kindMultiple" },
                                            { key: "typeName"},
                                            { key: "typeNodeRef"},
                                            { key: "caseName"},
                                            { key: "caseNodeRef"},
                                            { key: "uploader"},
                                            { key: "uploaded"},
                                            { key: "files" }
                                        ]
                                    }
                                });
                                this.documentKindsTable = new YAHOO.widget.DataTable(
                                    tableId,
                                    columnDefinitions,
                                    this.documentKindsDataSource,
                                    {}
                                );

                                me.addFilters(data, tableResult.rowList);
                                var table = Dom.get(tableId);
                                Event.addListener(table, "dragover", this.onDocumentKindsTableDragOver, this, true);
                                Event.addListener(table, "dragleave", this.onDocumentKindsTableDragLeave, this, true);
                                Event.addListener(table, "drop", this.onDocumentKindsTableDrop, this, true);
                            } else {
                                var bodyEl = Dom.get(this.id + "-body");
                                bodyEl.innerHTML = me.msg("empty-case-document");
                            }

                        } else {
                            var bodyEl = Dom.get(this.id + "-body");
                            bodyEl.innerHTML = me.msg("empty-case-document");
                        }
                    },

                    failure: function() {
                        Alfresco.logger.error("case documents request error");
                    },

                    scope: this
                }
            );
        },

        _refreshDoclibData: function CDU_refreshDoclibData(event, args) {
            var self = this;

            var record = args[1].record;
            var files = this.widgets.doclist.widgets.dataSource.liveData.items,
                remainingFilesNodeRefs = _.difference(_.pluck(files, "nodeRef"), [record.nodeRef]);
           
            this.recordsLoader.load(remainingFilesNodeRefs, function(nodeRefs, results) {
                self.widgets.doclist.loadData(results);
            });
        },

        addHeaderButtons: function CDU_addHeaderButtons() {
            var me = this;

            var uploadButtonEl = Dom.get(me.id + '-upload-button');
            if (uploadButtonEl) {
                new YAHOO.widget.Button({
                    id: me.id + '-upload-button',
                    type: 'button'
                });
                
                Event.addListener(
                    me.id + '-upload-button',
                    "click", function(event) {
                        me.onShowDialog();
                    }
                );
            }

            new YAHOO.widget.Button({
                id: me.id + '-update-button',
                type: 'button'
            });
            Event.addListener(
                me.id + '-update-button',
                "click", function(event) {
                    me.refreshDataTable();
                }
            );
        },

        addFilters: function CDU_addFilters(data, rowList) {
            var me = this;
            var caseFilter = new YAHOO.widget.Menu(
                me.id + "-cases-filter",
                {
                    position: "static"
                }
            );

            var onCaseFilterItemClick = function (p_sType, p_aArgs, p_oItem) {
                var item = YAHOO.util.Selector.query("#" + me.id + "-cases-filter li[index=" + me.caseFilterIndex + "]")[0];
                Dom.removeClass(item, "filter-selected");
                item = YAHOO.util.Selector.query("#" + me.id + "-cases-filter li[index=" + me.caseFilterIndex + "] a")[0];
                Dom.removeClass(item, "a-filter-selected");
                Dom.addClass(item, "yuimenuitemlabel");
                me.caseFilterValue = p_oItem.value;
                me.caseFilterIndex = p_oItem.index;
                var item = YAHOO.util.Selector.query("#" + me.id + "-cases-filter li[index=" + me.caseFilterIndex + "]")[0];
                Dom.addClass(item, "filter-selected");
                item = YAHOO.util.Selector.query("#" + me.id + "-cases-filter li[index=" + me.caseFilterIndex + "] a")[0];
                Dom.addClass(item, "a-filter-selected");
                Dom.removeClass(item, "yuimenuitemlabel");
                me.onFilter();
            };

            caseFilter.insertItem({
                'text': me.msg("filter.cases.all"),
                'value': "all",
                'onclick': { fn:  onCaseFilterItemClick}
            });

            var cases = me.searchCases(data.container, new Array());
            cases.sort(me.sortByName);
            for (var i in cases) {
                caseFilter.insertItem({
                    'text': cases[i].name,
                    'value': cases[i].nodeRef,
                    'onclick': { fn: onCaseFilterItemClick }
                });
            }
            caseFilter.render(me.id + "-cases-filter-div");
            caseFilter.show();
            var item = YAHOO.util.Selector.query("#" + me.id + "-cases-filter li[index=" + me.caseFilterIndex + "]")[0];
            Dom.addClass(item, "filter-selected");
            item = YAHOO.util.Selector.query("#" + me.id + "-cases-filter li[index=" + me.caseFilterIndex + "] a")[0];
            Dom.addClass(item, "a-filter-selected");
            Dom.removeClass(item, "yuimenuitemlabel");

            var onTypesFilterItemClick = function (p_sType, p_aArgs, p_oItem) {
                var item = YAHOO.util.Selector.query("#" + me.id + "-types-filter li[index=" + me.typeFilterIndex + "]")[0];
                Dom.removeClass(item, "filter-selected");
                item = YAHOO.util.Selector.query("#" + me.id + "-types-filter li[index=" + me.typeFilterIndex + "] a")[0];
                Dom.removeClass(item, "a-filter-selected");
                Dom.addClass(item, "yuimenuitemlabel");
                me.typeFilterValue = p_oItem.value;
                me.typeFilterIndex = p_oItem.index;
                item = YAHOO.util.Selector.query("#" + me.id + "-types-filter li[index=" + me.typeFilterIndex + "]")[0];
                Dom.addClass(item, "filter-selected");
                item = YAHOO.util.Selector.query("#" + me.id + "-types-filter li[index=" + me.typeFilterIndex + "] a")[0]
                Dom.addClass(item, "a-filter-selected");
                Dom.removeClass(item, "yuimenuitemlabel");
                me.onFilter();
            };

            var typeFilter = new YAHOO.widget.Menu(
                me.id + "-types-filter",
                {
                    position: "static"
                }
            );

            typeFilter.insertItem({
                'text': me.msg("filter.types.all"),
                'value': "all",
                'onclick': { fn: onTypesFilterItemClick }
            });

            var types = new Array();
            for (var i in rowList) {
                types.push({
                    name: rowList[i].typeName,
                    nodeRef: rowList[i].typeNodeRef
                });
            }

            types.sort(me.sortTypes);
            var i = types.length-1;

            while (types[i-1]) {
                if (types[i].nodeRef == types[i-1].nodeRef) {
                    types.splice(i, 1);
                }
                i--;
            }
            types.sort(me.sortByName);
            for (var i in types) {
                typeFilter.insertItem({
                    'text': types[i].name,
                    'value': types[i].nodeRef,
                    'onclick': { fn: onTypesFilterItemClick }
                });
            }
            typeFilter.render(me.id + "-types-filter-div");
            typeFilter.show();
            var item = YAHOO.util.Selector.query("#" + me.id + "-types-filter li[index=" + me.typeFilterIndex + "]")[0];
            Dom.addClass(item, "filter-selected");
            item = YAHOO.util.Selector.query("#" + me.id + "-types-filter li[index=" + me.typeFilterIndex + "] a")[0];
            Dom.addClass(item, "a-filter-selected");
            Dom.removeClass(item, "yuimenuitemlabel");

            Event.on(me.id + '-kinds-filter','keyup',function (e) {
                me.onFilter();
            });
            Event.on(me.id + '-uploaded-filter','click',function (e) {
                me.onFilter();
            });
            Event.on(me.id + '-mandatory-filter','click',function (e) {
                me.onFilter();
            });
        },

        onFilter: function CDU_onFilter() {
            var me = this;
            var caseFilter = me.caseFilterValue;
            var typeFilter = me.typeFilterValue;
            var kindFilter = Dom.get(me.id + '-kinds-filter').value;
            var uploadedFilter = Dom.get(me.id + '-uploaded-filter').checked;
            var mandatoryFilter = Dom.get(me.id + '-mandatory-filter').checked;

            var state = me.documentKindsTable.getState();
            me.documentKindsDataSource.sendRequest(null, {
                success : function(request, response, payload) {
                    this.initializeTable();

                    var rs = response.results;
                    var filtered = [];
                    for(var i = 0; i < rs.length; i++) {
                        if((caseFilter == "all" || rs[i]["caseNodeRef"] == caseFilter)
                            && (typeFilter == "all" || rs[i]["typeNodeRef"] == typeFilter)
                            && (kindFilter == "" || rs[i]["kindName"].toUpperCase().indexOf(kindFilter.toUpperCase()) + 1)
                            && (uploadedFilter == false || (uploadedFilter == true && rs[i]["status"] != "uploaded"))
                            && (mandatoryFilter == false || (mandatoryFilter == true && rs[i]["kindMandatory"] == true))) {
                            filtered[filtered.length] = rs[i];
                        }
                    }

                    this.getRecordSet().setRecords(filtered, 0);
                    
                    this.setAttributeConfig("MSG_EMPTY", {
                        value: me.msg("search.not-found")
                    });      
                    
                    
                    this.render();
                },
                failure : me.documentKindsTable.onDataReturnInitializeTable,
                scope   : me.documentKindsTable,
                argument: state
            });
        },
        
        addUploadDialog: function CDU_addUploadDialog() {
            var me = this;
            var caseSelect = new Array();

            // permissions
            var allowCreateType = me.data.permissions.createType;

            var onCaseMenuItemClickPostprocess = function(value) {
                    // clear type button
                    me.typeButtonMenu.getMenu().clearContent();
                    me.typeButtonMenu.set("label", me.msg("type-select"));

                    // enable type button
                    me.typeButtonMenu.set("disabled", false);
                    if (allowCreateType) me.typeCreateButton.set("disabled", false);

                    // clear kind menu button
                    me.kindButtonMenu.getMenu().clearContent();
                    me.kindButtonMenu.set("label", me.msg("kind-select"));

                    // disable kind button
                    me.kindButtonMenu.set("disabled", true);
                    me.kindCreateButton.set("disabled", true);

                    // disable file
                    if (!Dom.get("filedata").hasAttribute("disabled"))
                        Dom.get("filedata").setAttribute("disabled", "disabled");

                    for (var dt in me.data.documentTypes) {
                        var type = me.data.documentTypes[dt];

                        me.typeButtonMenu.getMenu().addItem({
                            'text': type.name,
                            'value': type.nodeRef,
                            'onclick': { fn: onTypeMenuItemClick }
                        });
                    }

                    me.typeButtonMenu.getMenu().render();
                },
                onCaseMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
                var sText = p_oItem.cfg.getProperty("text");
                me.caseButtonMenu.set("label", sText);
                onCaseMenuItemClickPostprocess(p_oItem.value);
            };

            var cases = me.searchCases(me.data.container, new Array());
            cases.sort(me.sortByName);

            for(var i in cases) {
                caseSelect.push({
                    'text': cases[i].name,
                    'value': cases[i].nodeRef + "_" + cases[i].kind,
                    'onclick': { fn: onCaseMenuItemClick }
                });
            }

            me.caseButtonMenu = new YAHOO.widget.Button(me.id + "-container", {
                type: "menu",
                label: me.msg("case-select"),
                name: "caseSelect",
                menu: caseSelect
            });

            var typeSelect = new Array();
            var types = me.data.documentTypes;
            types.sort(me.sortByName);

            var onTypeMenuItemClickPostprocess = function(value) {
                    // check permission to create kind
                    var allowCreateKind = false;
                    for (var t in types) {
                        if (types[t].nodeRef == value) {
                            allowCreateKind = types[t].permissions.createKind == "true";
                        }
                    }

                    // clear kind menu button
                    me.kindButtonMenu.getMenu().clearContent();
                    me.kindButtonMenu.set("label", me.msg("kind-select"));

                    // enable kind button
                    me.kindButtonMenu.set("disabled", false);
                    if (allowCreateKind) me.kindCreateButton.set("disabled", false);

                    // disable file
                    if (!Dom.get("filedata").hasAttribute("disabled"))
                        Dom.get("filedata").setAttribute("disabled", "disabled");

                    var items = me.getKindMenuItemsByType(me.data, value);
                    for (var i in items) {
                        me.kindButtonMenu.getMenu().insertItem({
                            'text': items[i].name,
                            'value': items[i].nodeRef,
                            'onclick': { fn: onKindMenuItemClick }
                        });
                    }

                    me.kindButtonMenu.getMenu().render();
                },
                onTypeMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
                    var sText = p_oItem.cfg.getProperty("text");
                    me.typeButtonMenu.set("label", sText);
                    onTypeMenuItemClickPostprocess(p_oItem.value);
                };

            for (var i in types) {
                typeSelect.push({
                    'text': types[i].name,
                    'value': types[i].nodeRef,
                    'onclick': { fn: onTypeMenuItemClick }
                });
            }

            me.typeButtonMenu = new YAHOO.widget.Button(me.id + "-type", {
                type: "menu",
                label: me.msg("type-select"),
                name: "typeSelect",
                menu: typeSelect,
                lazyloadmenu: false,
                disabled: true
            });
           
            // type create button
            me.typeCreateButton = new YAHOO.widget.Button({
                id: me.id + "-create-type",
                type: "push",
                label: "+",
                container: me.id + "-type-select-container",
                disabled: true
            })

            me.typeCreateButton.setStyle("margin-left", "15px");

            // type create button click event handler
            me.typeCreateButton.on("click", function() {
                Citeck.forms.dialog("cm:category", "type-kind", function(node) {
                    var subscription = node.impl().attributes.subscribe(function(newValue) {
                        if (node.name && node.nodeRef){
                            me.typeButtonMenu.getMenu().addItem({
                                text: node.name,
                                value: node.nodeRef,
                                onclick: { fn: onTypeMenuItemClick }
                            });

                            me.data.documentTypes.push({
                                nodeRef: node.nodeRef,
                                name: node.name
                            })
                           
                            subscription.dispose();
                        } 
                    });
                },
                { 
                    destination: "workspace://SpacesStore/category-document-type-root", 
                    destinationAssoc: "cm:subcategories" 
                })
            });

            var kindSelect = new Array();
            var kinds = me.data.documentKinds;
            kinds.sort(me.sortByName);

            var onKindMenuItemClickPostprocess = function(value) {
                    var caseNodeRef = (function() {
                            var caseSelectedElement = me.caseButtonMenu.get("selectedMenuItem");
                            if (typeof caseSelectedElement == "string") return caseSelectedElement;
                            return caseSelectedElement.value.split('_')[1]
                        })(),
                        caseContainer = _.findWhere(me.data.containerKinds, { nodeRef: caseNodeRef }),
                        containerKinds = caseContainer.documentKinds,
                        kind = _.findWhere(containerKinds, { nodeRef: value });
                    var filedataInput = Dom.get("filedata");

                    // enable file
                    if (filedataInput.hasAttribute("disabled")) filedataInput.removeAttribute("disabled");
                    
                    // set or remove multiple attribute
                    if (kind && kind.multiple) {
                        if (!filedataInput.hasAttribute("multiple")) filedataInput.setAttribute("multiple", "multiple"); 
                    } else {
                        if (!filedataInput.hasAttribute("multiple")) filedataInput.setAttribute("multiple", "multiple");
                        //if (filedataInput.hasAttribute("multiple")) filedataInput.removeAttribute("multiple");
                    }
                },
                onKindMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
                    var sText = p_oItem.cfg.getProperty("text");
                    me.kindButtonMenu.set("label", sText);
                    me.kindButtonMenu.set("selectedMenuItem", p_oItem.value);
                    onKindMenuItemClickPostprocess(p_oItem.value)
                };

            for (var i in kinds) {
                kindSelect.push({
                    'text': kinds[i].name,
                    'value': kinds[i].nodeRef,
                    'onclick': { fn: onKindMenuItemClick }
                });
            }

            me.kindButtonMenu = new YAHOO.widget.Button(me.id + "-kind", {
                type: "menu",
                label: me.msg("kind-select"),
                name: "kindSelect",
                menu: kindSelect,
                lazyloadmenu: false,
                disabled: true
            });

            // kind create button
            me.kindCreateButton = new YAHOO.widget.Button({
                id: me.id + "-create-kind",
                kind: "push",
                label: "+",
                container: me.id + "-kind-select-container",
                disabled: true
            });

            me.kindCreateButton.setStyle("margin-left", "15px");

            // kind create button click event handler
            me.kindCreateButton.on("click", function() {
                Citeck.forms.dialog("cm:category", "type-kind", function(node) {
                    var subscription = node.impl().attributes.subscribe(function(newValue) {
                        if (node.name && node.nodeRef){
                            var caseNodeRef = me.caseButtonMenu.get("selectedMenuItem").value.split('_')[0],
                                typeNodeRef = me.typeButtonMenu.get("selectedMenuItem").value;

                            for (var c in me.data.containerKinds) {
                                if (me.data.containerKinds[c].nodeRef == caseNodeRef) {
                                    me.data.containerKinds[c].documentKinds.push({
                                        nodeRef: node.nodeRef,
                                        multiple: true,
                                        mandatory: false
                                    })
                                }
                            }

                            me.data.documentKinds.push({
                                name: node.name,
                                nodeRef: node.nodeRef,
                                type: typeNodeRef
                            })
                            
                            me.kindButtonMenu.getMenu().addItem({
                                text: node.name,
                                value: node.nodeRef,
                                onclick: { fn: onKindMenuItemClick }
                            });

                            // simulate click
                            // onTypeMenuItemClick(null, null, me.typeButtonMenu.get("selectedMenuItem"));

                            subscription.dispose();
                        } 
                    });
                },
                { 
                    destination: me.typeButtonMenu.get("selectedMenuItem").value, 
                    destinationAssoc: "cm:subcategories" 
                })
            });

            var setDefaultValues = function() {
                // set default case
                var currentCase = _.find(cases, function(c) { return c.nodeRef == me.options.nodeRef });
                if (currentCase) {
                    me.caseButtonMenu.set("selectedMenuItem", currentCase.nodeRef);
                    me.caseButtonMenu.set("label", currentCase.name);
                    onCaseMenuItemClickPostprocess(currentCase.nodeRef);
                
                    // set default type
                    var currentType = _.find(types, function(t) {
                        return t.nodeRef == (me.options.documentUploadDefaultType || me.options.documentType);
                    });
                    
                    if (currentType) {
                        me.typeButtonMenu.set("selectedMenuItem", currentType.nodeRef);
                        me.typeButtonMenu.set("label", currentType.name);
                        onTypeMenuItemClickPostprocess(currentType.nodeRef);
                    
                        // set default kind
                        var kinds = me.getKindMenuItemsByType(me.data, currentType.nodeRef);
                            currentKind = _.find(kinds, function(k) {
                                if (me.options.documentUploadDefaultType) {
                                    if (me.options.documentUploadDefaultKind) {
                                        return k.nodeRef == me.options.documentUploadDefaultKind;
                                    }
                                    return;
                                }
                                return k.nodeRef == me.options.documentKind;
                            });
                       
                        if (currentKind) {
                            me.kindButtonMenu.set("selectedMenuItem", currentKind.nodeRef);
                            me.kindButtonMenu.set("label", currentKind.name);
                            onKindMenuItemClickPostprocess(currentKind.nodeRef);
                        }
                    }
                }
            };

            me.uploadDialog = new YAHOO.widget.Dialog(me.id + "-upload-dialog", {
                hideaftersubmit: true,
                width: "500px",
                fixedcenter: true,
                modal: true
            });

            me.uploadDialog.callback = {
                upload: function(responseUpload) {
                    // clear form
                    // me.caseButtonMenu.set("selectedMenuItem", null);
                    // me.caseButtonMenu._setLabel(me.msg("case-select"));
                    // me.typeButtonMenu.set("selectedMenuItem", null);
                    // me.typeButtonMenu._setLabel(me.msg("type-select"));
                    // me.kindButtonMenu.set("selectedMenuItem", null);
                    // me.kindButtonMenu._setLabel(me.msg("kind-select"));
                    
                    setDefaultValues();
                    Dom.get('filedata').value = null;

                    // active upload button
                    me.uploadDialog.getButtons()[0]._setDisabled(false);
                    me.uploadSuccessCallback(responseUpload);
                },
                failure: function(response) {
                    // active upload button
                    me.uploadDialog.getButtons()[0]._setDisabled(false);
                    me.uploadFailure();
                }
            };

            me.uploadDialog.cfg.queueProperty("buttons", [ 
                {
                    text: me.msg("dialog.button.upload"),
                    handler: function() {
                        var caseNode = (function() {
                                var caseSelectedElement = me.caseButtonMenu.get("selectedMenuItem");
                                if (typeof caseSelectedElement == "string") return caseSelectedElement;
                                return caseSelectedElement.value.split('_')[0];
                            })(),
                            typeNode = (function() {
                                var typeSelectedElement = me.typeButtonMenu.get("selectedMenuItem");
                                if (typeof typeSelectedElement == "string") return typeSelectedElement;
                                return typeSelectedElement.value;
                            })(),
                            kindNode = (function() {
                                var kindSelectedElement = me.kindButtonMenu.get("selectedMenuItem");
                                if (typeof kindSelectedElement == "string") return kindSelectedElement;
                                return kindSelectedElement.value;
                            })();

                        if(caseNode == null || typeNode == null || kindNode == null || Dom.get('filedata').value == "") {
                            Alfresco.util.PopupManager.displayMessage({
                                text: me.msg("error.not-selected-element")
                            });
                        } else {
                            Dom.get('container').setAttribute('value', caseNode);
                            Dom.get('type').setAttribute('value', typeNode);
                            Dom.get('kind').setAttribute('value', kindNode);
                            Dom.get('multiple').setAttribute('value', me.isKindMultiple(me.data, kindNode, caseNode));

                            if (Citeck.Browser.isIE(11)) {
                                me.uploadRequest = new XMLHttpRequest();
                                
                                var formData = new FormData;
                                formData.append("container", caseNode);
                                formData.append("type", typeNode);
                                formData.append("kind", kindNode);
                                formData.append("multiple", me.isKindMultiple(me.data, kindNode, caseNode));

                                for (var f in Dom.get("filedata").files) {
                                    formData.append("filedata-" + f, Dom.get("filedata").files[f]);
                                }

                                me.uploadRequest.open(
                                    Alfresco.util.Ajax.POST,  
                                    Alfresco.constants.PROXY_URI + 'citeck/cases/case-documents', 
                                    true
                                );

                                me.uploadRequest.upload.addEventListener("load", function(responseUpload) {
                                    if (me.uploadRequest.readyState != 4) {
                                        me.uploadRequest.onreadystatechange = function () {
                                            if (me.uploadRequest.readyState == 4) {
                                                // clear form
                                                // me.caseButtonMenu.set("selectedMenuItem", null);
                                                // me.caseButtonMenu._setLabel(me.msg("case-select"));
                                                // me.typeButtonMenu.set("selectedMenuItem", null);
                                                // me.typeButtonMenu._setLabel(me.msg("type-select"));
                                                // me.kindButtonMenu.set("selectedMenuItem", null);
                                                // me.kindButtonMenu._setLabel(me.msg("kind-select"));
                                                
                                                setDefaultValues();
                                                Dom.get('filedata').value = null;
                                                
                                                // active upload button
                                                me.uploadDialog.getButtons()[0]._setDisabled(false);
                                                me.requestCompletion(me.uploadRequest);
                                            }
                                        }
                                        me.uploadDialog.hide();
                                    } else {
                                        me.requestCompletion(me.uploadRequest);
                                    }
                                }, false);

                                me.uploadRequest.upload.addEventListener("error", me.uploadDialog.callback.failure, false);
                                me.uploadRequest.send(formData);
                            } else {
                                this.submit();
                            }

                            this.getButtons()[0]._setDisabled(true);
                        }
                    },
                    isDefault: true
                }, 
                {
                    text: me.msg("dialog.button.cancel"),
                    handler: function() {
                        this.cancel();
                    }
                } 
            ]);

            setDefaultValues();
        },
        
        addViewDialog: function() {
            this.widgets.viewDialog = new YAHOO.widget.Panel(this.id + "-view-dialog", {
                width: "800px",
                modal: true,
                visible: false,
                close: true,
                zindex: 4, 
                fixedcenter: true,
                draggable: true
            });

            this.widgets.viewDialog.render(document.body);
        },

        addIntermediateFileDialog: function() {
            this.widgets.intermediateFileDialog = new YAHOO.widget.Panel(this.id + "-intermediate-file-dialog", {
                width: "1025px",
                modal: true,
                visible: false,
                close: true,
                zindex: 4, 
                fixedcenter: true,
                draggable: true
            });

            this.widgets.intermediateFileDialog.setBody("")            
            this.widgets.intermediateFileDialog.render(document.body);

            this.widgets.intermediateFileDialog.hideMaskEvent.subscribe(function() {
                $("html").css("overflow-y", "auto");
            })

            this.widgets.intermediateFileDialog.showMaskEvent.subscribe(function() {
                $("html").css("overflow-y", "hidden");
            })
        },
        
        isKindMultiple: function(data, kindNode, caseNode) {
            var container = this.getContainer(data.container, caseNode),
                containerKind = _.findWhere(data.containerKinds, { nodeRef: container.kind }),
                documentKind = _.findWhere(containerKind.documentKinds, { nodeRef: kindNode });

            return documentKind ? documentKind.multiple : true;
        },
        
        getContainer: function(container, caseNode) {
            if(container.nodeRef == caseNode) return container;
            for(var i in container.containers) {
                var childContainer = this.getContainer(container.containers[i], caseNode);
                if(childContainer) return childContainer;
            }
            return null;
        },

        onShowDialog: function CDU_onShowDialog() {
            if(!this.uploadDialog._rendered) {
                this.uploadDialog.render(document.body);
            } else {
                this.uploadDialog.show();
            }
        },

        refreshDataTable: function CDU_refreshDataTable() {
            var me = this;
            var getCaseDocumentsUrl = Alfresco.constants.PROXY_URI + 'citeck/cases/case-documents?nodeRef=' + this.options.nodeRef;
            YAHOO.util.Connect.asyncRequest(
                'GET',
                getCaseDocumentsUrl, {
                    success: function (response) {
                        if (response.responseText) {
                            var data = me.data = Alfresco.util.parseJSON(response.responseText);

                            var tableResult = { rowList : new Array()};
                            tableResult.rowList = me.searchDocumentKinds(data.container, tableResult.rowList,
                                data.containerKinds, data.documentTypes, data.documentKinds);
                            tableResult.rowList.sort(me.sortTable);

                            me.documentKindsTable.showTableMessage("Loading...");
                            me.documentKindsTable.getDataSource().liveData = tableResult;
                            me.documentKindsTable.getDataSource().sendRequest(null, {
                                success: function(sRequest, oResponse, oPayload) {
                                    me.documentKindsTable.onDataReturnInitializeTable.call(me.documentKindsTable, sRequest, oResponse, oPayload);
                                },
                                failure: function(sRequest, oResponse) {
                                    if (oResponse.status == 401) {
                                        window.location.reload();
                                    } else {
                                        me.documentKindsTable.set("MSG_ERROR", "datasurce error");
                                        me.documentKindsTable.showTableMessage("datasurce error", YAHOO.widget.DataTable.CLASS_ERROR);
                                    }
                                },
                                scope: me
                            });

                            if(tableResult.rowList.length ==0 ){
                                var bodyEl = Dom.get(this.id + "-body");
                                bodyEl.innerHTML = me.msg("empty-case-document");
                            }

                            me.onFilter();
                        }
                    },
                    failure: function() {
                        Alfresco.util.PopupManager.displayPrompt(
                        {
                            title: me.msg("update-error"),
                            text: me.msg("update-error.can-not-be-updated"),
                            noEscape: true,
                            buttons: [
                                {
                                    text: me.msg("button.ok"),
                                    handler: function dlA_onAction_cancel()
                                    {
                                        this.destroy();
                                    }
                                }]
                        });
                    },
                    scope: me
                }
            );
        },

        uploadSuccessCallback: function CDU_uploadSuccessCallback(responseUpload) {
            var me = this;

            // popup message about success was uploaded
            Alfresco.util.PopupManager.displayMessage({ text: me.msg("upload-ok.success") });

            var dataUploaded = Alfresco.util.parseJSON(responseUpload.responseText);
            var state = me.documentKindsTable.getState();

            // extend data. set uploaded files to case documents
            var documents = _.map(dataUploaded.documents, function(item) {
                    return _.extend(item, {
                        kind: dataUploaded.kind,
                        type: dataUploaded.type,
                        uploaded: dataUploaded.uploaded,
                        uploader: dataUploaded.uploader
                    });
                });

            if (me.data.container.nodeRef == me.caseUploadNodeRef) {
                me.data.container.documents = _.union(me.data.container.documents, documents); 
            } else {
                var container = _.findWhere(me.data.container.containers, { nodeRef: me.caseUploadNodeRef });
                if (container) { 
                    container.documents = _.union(container.documents, documents);
                }
            }

            me.documentKindsDataSource.sendRequest(null, {
                success : function(request, response, payload) {
                    this.initializeTable();

                    var rs = response.results;
                    var filtered = [];
                    var rowNotExist = true;

                    for(var i = 0; i < rs.length; i++) {
                        if(rs[i]["caseNodeRef"] == me.caseUploadNodeRef
                            && rs[i]["typeNodeRef"] == dataUploaded.type
                            && rs[i]["kindNodeRef"] == dataUploaded.kind) {

                            rowNotExist = false;

                            me.documentKindsDataSource.liveData.rowList[i]["status"] = "uploaded";
                            me.documentKindsDataSource.liveData.rowList[i]["uploaded"] = Alfresco.util.formatDate(dataUploaded.uploaded, "dd.mm.yyyy HH:MM");
                            me.documentKindsDataSource.liveData.rowList[i]["uploader"] = me.getUploader(dataUploaded.uploader.userName,
                                dataUploaded.uploader.firstName, dataUploaded.uploader.lastName);
                            
                            var files = me.documentKindsDataSource.liveData.rowList[i]["files"];
                            for (var d in dataUploaded.documents) {
                                var file = _.findWhere(files, { nodeRef: dataUploaded.documents[d].nodeRef });
                                if (!file) { 
                                    file = { 
                                        nodeRef: dataUploaded.documents[d].nodeRef, 
                                        name: dataUploaded.documents[d].name 
                                    }
                                    files.push(file);
                                }

                                _.extend(file, {
                                    kind: dataUploaded.kind,
                                    type: dataUploaded.type,
                                    uploaded: dataUploaded.uploaded,
                                    uploader: dataUploaded.uploader
                                });                           
                            }
                        }

                        filtered[filtered.length] = rs[i];
                    }

                    if (rowNotExist) {
                        me.refreshDataTable.call(me);
                    } else {
                        this.getRecordSet().setRecords(filtered, 0);
                        this.render();

                        me.onFilter();
                    }
                },
                failure : me.documentKindsTable.onDataReturnInitializeTable,
                scope   : me.documentKindsTable,
                argument: state
            });
        },

        searchDocumentKinds: function CDU_searchDocumentKinds(container, rowList, containerKinds, documentTypes, documentKinds) {
            var me = this;
            if (container.kind != null) {
                for (var i in containerKinds) {
                    if (containerKinds[i].nodeRef == container.kind) {
                        var containerDocumentKinds = containerKinds[i].documentKinds;
                        for (var j in containerDocumentKinds) {
                            if(containerDocumentKinds[j].containerType) {
                                if(typeof containerDocumentKinds[j].containerType == "string"
                                    && containerDocumentKinds[j].containerType != container.type) {
                                    continue;
                                } else if (typeof containerDocumentKinds[j].containerType == "object"
                                    && containerDocumentKinds[j].containerType.indexOf(container.type) == -1) {
                                    continue;
                                }
                            }
                            var documentKind = {
                                caseKind: null,
                                caseNodeRef: null,
                                caseName: null,
                                kindNodeRef: null,
                                kindName: null,
                                kindMandatory: null,
                                kindMultiple: null,
                                typeNodeRef: null,
                                typeName: null,
                                status: null,
                                files: [],
                                uploaded: null,
                                uploader: null
                            };
                            documentKind.caseKind = container.kind;
                            documentKind.caseNodeRef = container.nodeRef;
                            documentKind.caseName = container.name;
                            documentKind.kindNodeRef = containerDocumentKinds[j].nodeRef;
                            documentKind.kindMultiple = containerDocumentKinds[j].multiple;
                            documentKind.kindMandatory = containerDocumentKinds[j].mandatory;
                            documentKind.status = documentKind.kindMandatory ? "notUploadedMandatory" : "notUploaded";

                            for (var k in documentKinds) {
                                if (documentKinds[k].nodeRef == containerDocumentKinds[j].nodeRef) {
                                    documentKind.kindName = documentKinds[k].name;
                                    documentKind.typeNodeRef = documentKinds[k].type;

                                    for (var t in documentTypes) {
                                        if (documentTypes[t].nodeRef == documentKinds[k].type) {
                                            documentKind.typeName = documentTypes[t].name;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }

                            for (var l in container.documents) {
                                if (container.documents[l].kind == documentKind.kindNodeRef) {
                                    documentKind.status = "uploaded";
                                    documentKind.uploaded = Alfresco.util.formatDate(container.documents[l].uploaded, "dd.mm.yyyy HH:MM");
                                    documentKind.uploader = me.getUploader(container.documents[l].uploader.userName,
                                        container.documents[l].uploader.firstName, container.documents[l].uploader.lastName);
                                    
                                    documentKind.files.push(container.documents[l]);
                                }
                            }
                            rowList.push(documentKind);
                        }
                        break;
                    }
                }
            }

            var items = container.containers;
            for(var i in items) {
                me.searchDocumentKinds(items[i], rowList, containerKinds, documentTypes, documentKinds);
            }
            return rowList;
        },

        searchCases: function CDU_searchCases(container, casesList) {
            var me = this;
            casesList.push({
                name : container.name,
                nodeRef: container.nodeRef,
                kind: container.kind
            });

            var items = container.containers;
            for(var i in items) {
                me.searchCases(items[i], casesList);
            }
            return casesList;
        },

        getMenuItemsByCase: function CDU_getMenuItemsByCase(data, caseKindNodeRef) {
            var me = this;
            var kinds = [], types = [];

            if (caseKindNodeRef != null) {
                for (var i in data.containerKinds) {
                    if (data.containerKinds[i].nodeRef == caseKindNodeRef) {
                        var containerDocumentKinds = data.containerKinds[i].documentKinds;
                        for (var j in containerDocumentKinds) {
                            for (var k in data.documentKinds) {
                                if (data.documentKinds[k].nodeRef == containerDocumentKinds[j].nodeRef) {
                                    kinds.push({name: data.documentKinds[k].name, nodeRef: containerDocumentKinds[j].nodeRef});
                                    for (var t in data.documentTypes) {
                                        if (data.documentTypes[t].nodeRef == data.documentKinds[k].type) {
                                            types.push({name: data.documentTypes[t].name, nodeRef: data.documentKinds[k].type});
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            types.sort(me.sortTypes);
            var i = types.length-1;

            while (types[i-1]) {
                if (types[i].nodeRef == types[i-1].nodeRef) {
                    types.splice(i, 1);
                }
                i--;
            }

            kinds.sort(me.sortByName);
            types.sort(me.sortByName);

            return { kinds: kinds, types: types };
        },

        getKindMenuItemsByType: function CDU_getKindMenuItemsByType(data, typeNodeRef, caseNodeRef) {
            var me = this,
                kinds = [];

            if (typeNodeRef != null) {
                if (caseNodeRef != null) {
                    for (var i in data.containerKinds) {
                        if (data.containerKinds[i].nodeRef == caseNodeRef) {
                            var containerDocumentKinds = data.containerKinds[i].documentKinds;
                            for (var j in containerDocumentKinds) {
                                for (var k in data.documentKinds) {
                                    if (data.documentKinds[k].nodeRef == containerDocumentKinds[j].nodeRef
                                        && data.documentKinds[k].type == typeNodeRef) {

                                        kinds.push(data.documentKinds[k]);
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else {
                    for (var dk in data.documentKinds) {
                        if (data.documentKinds[dk].type == typeNodeRef) 
                            kinds.push(data.documentKinds[dk])
                    }
                }
            } 

            return kinds.sort(me.sortByName);
        },


        getUploader: function CDU_getUploader(userName, firstName, lastName) {
            var displayName = userName;
            if ((firstName !== undefined && firstName.length > 0) ||
                (lastName !== undefined && lastName.length > 0)) {

                displayName = '';
                if (firstName !== undefined) {
                    displayName = firstName + ' ';
                }
                if (lastName !== undefined) {
                    displayName += lastName;
                }
            }
            return displayName;
        },

        sortTable: function CDU_sortTable(a, b){
            if (a.caseName < b.caseName){
                return -1;
            } else if (a.caseName > b.caseName) {
                return  1;
            } else{
                if (a.typeName < b.typeName) {
                    return -1;
                } else if (a.typeName > b.typeName) {
                    return 1;
                } else {
                    if (a.kindName < b.kindName) {
                        return -1;
                    } else if (a.kindName > b.kindName) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        },

        sortByName: function CDU_sortByName(a, b){
            if (a.name < b.name){
                return -1;
            } else if (a.name > b.name) {
                return  1;
            } else {
                return 0;
            }
        },

        sortTypes: function CDU_sortTypes(a, b){
            if (a.nodeRef < b.nodeRef){
                return -1;
            } else if (a.nodeRef > b.nodeRef) {
                return  1;
            } else {
                return 0;
            }
        },

        onDocumentKindsTableDragOver: function CDU_onDocumentKindsTableDragOver(e)
        {
            var tableRow = this.documentKindsTable.getRow(e.target);
            Dom.setStyle(tableRow, 'outline', '1px solid #5C5C5C');
            // Firefox 3.6 set effectAllowed = "move" for files, however the "copy" effect is more accurate for uploads
            e.dataTransfer.dropEffect = Math.floor(YAHOO.env.ua.gecko) === 1 ? "move" : "copy";
            e.stopPropagation();
            e.preventDefault();
        },

        onDocumentKindsTableDragLeave: function CDU_onDocumentKindsTableDragLeave(e)
        {
            var tableRow = this.documentKindsTable.getRow(e.target);
            Dom.setStyle(tableRow, 'outline', 'none');
            e.stopPropagation();
            e.preventDefault();
        },

        onDocumentKindsTableDrop: function CDU_onDocumentKindsTableDrop(e)
        {
            var me = this;
            try {
                // Only perform a file upload if the user has *actually* dropped some files!
                if (e.dataTransfer.files !== undefined && e.dataTransfer.files !== null && e.dataTransfer.files.length > 0) {
                    var continueWithUpload = false;
                    // Check that at least one file with some data has been dropped...
                    var zeroByteFiles = "", i, j;

                    j = e.dataTransfer.files.length;
                    for (i = 0; i < j; i++) {
                        if (e.dataTransfer.files[i].size > 0) {
                            continueWithUpload = true;
                        } else {
                            zeroByteFiles += '"' + e.dataTransfer.files[i].fileName + '", ';
                        }
                    }

                    if (continueWithUpload) {
                        var oRecord = me.documentKindsTable.getRecord(e.target).getData();
                        me.caseUploadNodeRef = oRecord.caseNodeRef;

                        var successListener = function (e) {
                            if (me.dndRequest.readyState != 4) {
                                me.dndRequest.onreadystatechange = function () {
                                    if (me.dndRequest.readyState == 4) {
                                        me.requestCompletion(me.dndRequest);
                                    }
                                }
                            } else {
                                me.requestCompletion(me.dndRequest);
                            }
                        };

                        me.dndRequest = new XMLHttpRequest();
                        me.dndRequest.upload.addEventListener("load", successListener, false);
                        me.dndRequest.upload.addEventListener("error", me.uploadFailure, false);

                        var formData = new FormData;
                        formData.append("container", oRecord.caseNodeRef);
                        formData.append("type", oRecord.typeNodeRef);
                        formData.append("kind", oRecord.kindNodeRef);
                        formData.append("multiple", oRecord.kindMultiple);

                        for (var f in e.dataTransfer.files) {
                            formData.append("filedata-" + f, e.dataTransfer.files[f]);
                        }

                        var postCaseDocumentsUrl = Alfresco.constants.PROXY_URI + 'citeck/cases/case-documents';
                        me.dndRequest.open("POST",  postCaseDocumentsUrl, true);
                        me.dndRequest.send(formData);
                    }
                }
            }
            catch(exception)
            {
                Alfresco.logger.error("CDU_onDocumentKindsTableDrop: The following error occurred when files were dropped onto the Document List: ", exception);
            }
            e.stopPropagation();
            e.preventDefault();
        },

        requestCompletion: function CDU_requestCompletion(request) {
            if (request.status == "200") {
                this.uploadSuccessCallback(request);
            } else {
                this.uploadFailure();
            }
        },

        uploadFailure: function() {
            var self = this;
            Alfresco.util.PopupManager.displayPrompt({
                title: self.msg("upload-error"),
                text: self.msg("upload-error.can-not-be-linked"),
                noEscape: true,
                buttons: [{
                    text: self.msg("button.ok"),
                    handler: function dlA_onAction_cancel() {
                        this.destroy();
                    }
                }]
            }); 
        }
    });


    // Control for renter interme
    Citeck.widget.intermediateFileDialog = function(htmlId) {
        Citeck.widget.intermediateFileDialog.superclass.constructor.call(this, "Citeck.widget.intermediateFileDialog", htmlId);
    };

    YAHOO.extend(Citeck.widget.intermediateFileDialog, Alfresco.component.Base, {

        //  DEFAULT OPTIONS
        options: {
            panel: null,
            file: null,
            propertiesFormId: null,

            callback: {
                afterDelete: null
            },
        },

        onReady: function() {
            var self = this,
                rootNode = Dom.get(this.id),
                bodyNode = Dom.getElementsByClassName("bd", "div", rootNode)[0] || rootNode;

            if (this.options.panel && this.options.file) {
                // clear body before put new content
                this._clearDocument();

                // remember overlay
                self.mask = self.options.panel.mask;

                // initialize blocks
                var previewBlock = $("<div>", { "id": self.id + "-preview", "class": "preview" }),
                    propertiesBlock = $("<div>", { "id": self.id + "-properties", "class": "properties" }),
                    versionsBlock = $("<div>", { "id": self.id + "-version", "class": "versions" }),
                    versionsComparisonBlock = $("<div>", { "id": self.id + "-version-comparison", "class": "versions-comparison" }),
                    actionsBlock = $("<div>", { "class": "actions"});

                // create CRUD actions
                actionsBlock.append(this._createActionsSet());

                $(bodyNode)
                    .append(previewBlock)
                    .append(actionsBlock)
                    .append(propertiesBlock)
                    .append(
                        $("<div>", { 
                            "html": Alfresco.messages.scope["Citeck.widget.CaseDocumentsUploader"]["section.versions"], 
                            "class": "intermediate-dialog-title",
                            "style": "margin-top: 0px;" 
                        })
                    )
                    .append(versionsBlock)
                    .append(
                        $("<div>", { 
                            "html": Alfresco.messages.scope["Citeck.widget.CaseDocumentsUploader"]["section.versionsComparison"], 
                            "class": "intermediate-dialog-title" 
                        })
                    )
                    .append(versionsComparisonBlock);

                // Render the properties
                this._renderProperties(propertiesBlock.attr("id"))

                // Render the web-preview
                Alfresco.util.loadWebscript({
                  url: Alfresco.constants.URL_SERVICECONTEXT + "components/preview/web-preview",
                  properties: { nodeRef: this.options.file.nodeRef },
                  target: previewBlock.attr("id")
                });

                // Render the versions
                Alfresco.util.loadWebscript({
                    url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/components/document-versions-minimalistic",
                    properties: { nodeRef: this.options.file.nodeRef },
                    target: versionsBlock.attr("id")
                });

                // Render the versions-comparison
                Alfresco.util.loadWebscript({
                    url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/components/document-versions-comparison",
                    properties: { nodeRef: this.options.file.nodeRef },
                    target: versionsComparisonBlock.attr("id")
                });
            } else { /* TODO: no content or error message */ }
        },

        _clearDocument: function() {
            var rootNode = Dom.get(this.id),
                bodyNode = Dom.getElementsByClassName("bd", "div", rootNode)[0] || rootNode;

            $(bodyNode).html("");
            $(".web-preview.real").remove();
        },

        _renderProperties: function(elementId) {
            var itemId = this.options.file.nodeRef,
                formId = this.options.propertiesFormId || ""; 

            var newForm = function() {
                Alfresco.util.loadWebscript({
                    url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/components/node-view",
                    properties: { 
                        nodeRef: itemId,
                        mode: "view",
                        viewId: formId
                    },
                    target: elementId
                })
            };

            var oldForm = function() {
                Alfresco.util.loadWebscript({
                    url: Alfresco.constants.URL_SERVICECONTEXT + "components/form",
                    properties: {
                        itemKind: "node",
                        itemId: itemId,
                        mode: "view",
                        formId: formId
                    },
                    target: elementId
                })
            };

            var checkUrl = YAHOO.lang.substitute(Alfresco.constants.PROXY_URI + "citeck/invariants/view-check?nodeRef={itemId}&viewId={formId}&mode=view", {
                itemId: itemId,
                formId: formId
            });

            Alfresco.util.Ajax.jsonGet({
                url: checkUrl,
                successCallback: { fn: function(response) {
                    if(response.json.exists) {
                        newForm();
                    } else if(response.json.defaultExists) {
                        formId = "";
                        newForm();
                    } else {
                        oldForm();
                    }
                }},
                failureCallback: { fn: function(response) {
                    oldForm();
                }}
            });
        },

        _createActionsSet: function() {
            var self = this;

            var downloadUrl = Alfresco.constants.PROXY_URI + "/citeck/print/content?nodeRef=" + this.options.file.nodeRef,
                downloadImage = Alfresco.constants.URL_RESCONTEXT + "/components/documentlibrary/actions/document-download-16.png";

            var documentViewDetailsUrl = Alfresco.constants.URL_PAGECONTEXT + "card-details?nodeRef=" + this.options.file.nodeRef,
                documentViewDetailsImage = Alfresco.constants.URL_RESCONTEXT + "/components/documentlibrary/actions/document-view-details-16.png";

            var documentEditPropertiesUrl = Alfresco.constants.URL_PAGECONTEXT + "edit-metadata?nodeRef=" + this.options.file.nodeRef,
                documentEditPropertiesImage = Alfresco.constants.URL_RESCONTEXT + "/components/documentlibrary/actions/document-edit-properties-16.png";

            var documentDeleteImage = Alfresco.constants.URL_RESCONTEXT + "/components/documentlibrary/actions/document-delete-16.png",
                uploadImage = Alfresco.constants.URL_RESCONTEXT + "/components/documentlibrary/actions/document-upload-new-version-16.png";

            return $("<div>", { "class": "action-set" })
                .append(
                    $("<div>", { "class": "document-download" })
                        .append($("<a>", { 
                                "class": "simple-link",
                                "href": downloadUrl, 
                                "style": "background-image: url(" + downloadImage + ")",
                                "title": self.msg("actions.document.download"),
                                "html": self.msg("actions.document.download")
                            })
                        )
                )
                .append(
                    $("<div>", { "class": "document-view-details" })
                        .append($("<a>", { 
                                "class": "simple-link", 
                                "href": documentViewDetailsUrl, 
                                "style": "background-image: url(" + documentViewDetailsImage + ")",
                                "title": self.msg("actions.folder.view-details"),
                                "html": self.msg("actions.folder.view-details")
                            })
                        )
                )
                .append(
                    $("<div>", { "class": "document-edit-properties" })
                        .append($("<a>", { 
                                "class": "simple-link", 
                                "href": documentEditPropertiesUrl, 
                                "style": "background-image: url(" + documentEditPropertiesImage + ")",
                                "title": self.msg("actions.document.edit-metadata"),
                                "html": self.msg("actions.document.edit-metadata")
                            })
                        )
                )
                .append(
                    $("<div>", { "class": "document-delete" })
                        .append($("<a>", { 
                                "id": "onActionDelete",
                                "class": "action-link", 
                                "href": "#", 
                                "style": "background-image: url(" + documentDeleteImage + ")",
                                "title": self.msg("menu.selected-items.delete"),
                                "html": self.msg("menu.selected-items.delete")
                            }).click(function() {
                                Alfresco.util.PopupManager.displayPrompt({
                                    title: self.msg("button.delete"),
                                    text: self.msg("dialog.delete-confirmation").replace("{0}", '"' + self.options.file.name + '"'),
                                    nodeRef : self.options.file.nodeRef,
                                    noEscape: true,
                                    buttons: [
                                        {
                                            text: self.msg("button.delete"),
                                            handler: function() {
                                                this.hide();

                                                self.waitMessage = Alfresco.util.PopupManager.displayMessage({
                                                    text: self.msg("message.multiple-delete.please-wait"),
                                                    displayTime: 0,
                                                    modal: true
                                                });

                                                // set intermediate dialog mask for wait message
                                                Dom.setStyle(self.mask, "z-index", self.waitMessage.cfg.getProperty("zIndex") - 1);

                                                YAHOO.util.Connect.asyncRequest(
                                                    'DELETE',
                                                    Alfresco.constants.PROXY_URI + "/citeck/node?nodeRef=" + self.options.file.nodeRef, {
                                                        success: function (response) {
                                                            // hide popup and intermediate panel
                                                            self.options.panel.hide();

                                                            // clear document 
                                                            self._clearDocument();

                                                            // fire event about file deleted
                                                            YAHOO.Bubbling.fire("fileDeleted");

                                                            // destroy wait message
                                                            self.waitMessage.destroy();

                                                            // show success message
                                                            self.message = Alfresco.util.PopupManager.displayMessage({
                                                                text: self.msg("message.delete.success").replace("{0}", self.options.file.name)
                                                            });

                                                            // destroy success message
                                                            setTimeout(function() {
                                                                if (self.message) self.message.destroy();
                                                            }, 2000)
                                                        },
                                                        failure: function() {
                                                            // destroy wait message
                                                            self.waitMessage.destroy();

                                                            // show failure message
                                                            self.message = Alfresco.util.PopupManager.displayMessage({
                                                                text: self.msg("message.delete.failure").replace("{0}", self.options.file.name)
                                                            });

                                                            // destroy failure message
                                                            setTimeout(function() {
                                                                if (self.message) self.message.destroy();
                                                            }, 2000);

                                                            // restore intermediate dialog mask
                                                            Dom.setStyle(self.mask, "z-index", self.options.panel.cfg.getProperty("zIndex") - 1);

                                                            // logger
                                                            Alfresco.logger.error("deleting document error");
                                                        },

                                                        scope: this
                                                    }
                                                );

                                                this.destroy();
                                            }
                                        },
                                        {
                                            text: self.msg("button.cancel"),
                                            handler: function() {
                                                this.destroy();
                                            },
                                            isDefault: true
                                        }
                                    ]
                                })
                            })
                        )
                );
        }
    });

})();


Alfresco.WebPreview.prototype.Plugins.WebPreviewer.prototype.attributes.showFullWindowButton = false;

Alfresco.WebPreview.prototype.Plugins.WebPreviewer.prototype.display = function WebPreviewer_display() {
    var ctx = this.resolveUrls();

    this.createSwfDiv();

    var swfId = "WebPreviewer_" + this.wp.id;
    var so = new YAHOO.deconcept.SWFObject(Alfresco.constants.URL_CONTEXT + "res/components/preview/WebPreviewer.swf",
        swfId, "100%", "100%", "9.0.45");

    so.addVariable("fileName", this.wp.options.name);
    so.addVariable("paging", this.attributes.paging);
    so.addVariable("url", ctx.url);
    so.addVariable("jsCallback", "Alfresco_WebPreview_WebPreviewerPlugin_onWebPreviewerEvent");
    so.addVariable("jsLogger", "Alfresco_WebPreview_WebPreviewerPlugin_onWebPreviewerLogging");
    so.addVariable("i18n_actualSize", this.wp.msg("preview.actualSize"));
    so.addVariable("i18n_fitPage", this.wp.msg("preview.fitPage"));
    so.addVariable("i18n_fitWidth", this.wp.msg("preview.fitWidth"));
    so.addVariable("i18n_fitHeight", this.wp.msg("preview.fitHeight"));
    so.addVariable("i18n_fullscreen", this.wp.msg("preview.fullscreen"));
    so.addVariable("i18n_fullwindow", this.wp.msg("preview.fullwindow"));
    so.addVariable("i18n_fullwindow_escape", this.wp.msg("preview.fullwindowEscape"));
    so.addVariable("i18n_page", this.wp.msg("preview.page"));
    so.addVariable("i18n_pageOf", this.wp.msg("preview.pageOf"));

    so.addVariable("show_fullscreen_button", this.attributes.showFullScreenButton);
    so.addVariable("show_fullwindow_button", this.attributes.showFullWindowButton);
    so.addVariable("disable_i18n_input_fix", this.disableI18nInputFix());

    so.addParam("allowNetworking", "all");
    so.addParam("allowScriptAccess", "sameDomain");
    so.addParam("allowFullScreen", "true");
    so.addParam("wmode", "transparent");

    so.write(this.swfDiv.get("id"));  

    this.synchronizeSwfDivPosition();
};

Alfresco.WebPreview.prototype.Plugins.WebPreviewer.prototype.createSwfDiv = function WebPreviewer_createSwfDiv() {    
    if (!this.swfDiv) {
        var realSwfDivEl = new YAHOO.util.Element(document.createElement("div"));
        realSwfDivEl.set("id", this.wp.id + "-full-window-div");
        realSwfDivEl.setStyle("position", "absolute");
        realSwfDivEl.addClass("web-preview");
        realSwfDivEl.addClass("real");
        
        var realSwfContainerDivEl = new YAHOO.util.Element(document.getElementById(this.wp.id + "-previewer-div").parentNode);
        realSwfContainerDivEl.setStyle("position", "relative");
        realSwfDivEl.appendTo(realSwfContainerDivEl);

        this.swfDiv = realSwfDivEl;
    }
};   

Alfresco.WebPreview.prototype.Plugins.WebPreviewer.prototype.synchronizeSwfDivPosition = function WebPreviewer_synchronizePosition() {
    var sourceYuiEl = new YAHOO.util.Element(this.wp.getPreviewerElement());
    var region = YAHOO.util.Dom.getRegion(sourceYuiEl.get("id"));

    var sourceElement = YAHOO.util.Dom.get(sourceYuiEl.get("id")),
        containerElement = sourceElement, zindex;

    if (containerElement) {
        while (containerElement.tagName != "BODY") {
          if (containerElement.classList.contains("yui-panel-container") && containerElement.id.indexOf("intermediate-file-dialog") != -1) {
            this.swfDiv.setStyle("z-index", containerElement.style.zIndex);
            break;
          }

          containerElement = containerElement.parentNode;
        }

        this.swfDiv.setStyle("left", 0 + "px");
        this.swfDiv.setStyle("top", 0 + "px");
        this.swfDiv.setStyle("width", region.width + "px");
        this.swfDiv.setStyle("height", region.height + "px");
    }
};