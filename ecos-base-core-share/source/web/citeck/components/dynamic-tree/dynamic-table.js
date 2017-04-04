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
 * DynamicTable and DynamicTableControl classes defined here.
 */
(function() {

	Citeck = typeof Citeck != "undefined" ? Citeck : {};
	Citeck.widget = Citeck.widget || {};

	var Dom = YAHOO.util.Dom;

	/**
	 * DynamicTable class.
	 * It is view of the MVC pattern.
	 * 
	 * Supported messages:
	 * message.dynamic-table.empty
	 * message.dynamic-table.error
	 * message.dynamic-table.loading
	 * message.dynamic-table.sortasc
	 * message.dynamic-table.sortdesc
	 */
	Citeck.widget.DynamicTable = function(htmlid, model, name) {

		Citeck.widget.DynamicTable.superclass.constructor.call(this, name || "Citeck.widget.DynamicTable", htmlid);

		this.state = {};
		this.defaultData = {
			items: [],
			startIndex: 0,
			totalRecords: 0
		};
		this.defaultResponseSchema = {
			resultsList: "items",
			fields: [] 
		};
		this.defaultResponseType = YAHOO.util.DataSource.TYPE_JSON;
		this.defaultColumnConfig = [];
		this.model = model;
		this.loaded = false;
		this.deferredItems = null;
		this.config = {};

		// subscribe on model updates:
		model.subscribe("itemUpdated", this.onItemUpdated, this, true);
		model.subscribe("childrenUpdated", this.onChildrenUpdated, this, true);
		model.subscribe("childAdded", this.onChildAdded, this, true);
		model.subscribe("childDeleted", this.onChildDeleted, this, true);

		this.createEvent("sortColumnChange");

		return this;
	};

	Citeck.widget.DynamicTable.CLASS_NAME = "dynamic-table";

	YAHOO.extend(Citeck.widget.DynamicTable, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.DynamicTable.prototype, YAHOO.util.EventProvider.prototype);
	YAHOO.lang.augmentObject(Citeck.widget.DynamicTable.prototype, {

		/**
		 * Set table configuration.
		 * See columns configuration: 
		 *    http://yui.github.io/yui2/docs/yui_2.9.0_full/datatable/index.html#instantiating
		 *    http://yui.github.io/yui2/docs/yui_2.9.0_full/docs/YAHOO.widget.DataTable.html
		 * See responseSchema configuration:
		 *    http://yui.github.io/yui2/docs/yui_2.9.0_full/datasource/index.html#schemas , section JSON
		 *    http://yui.github.io/yui2/docs/yui_2.9.0_full/docs/YAHOO.util.DataSourceBase.html
		 * configuration can contain:
		 * columns: [
		 *    {key: "col1", label: "Column 1"},
		 *    {key: "col2", label: "Column 2"},
		 *    ...
		 * ],
		 * responseSchema: {
		 *    resultsList: "see.example", // Dot-notation location to results. Not all data is required to have a field.
		 *    fields: [{key: "id"}, {key: "obj.nested"}], // Array. String locator for each field of data coming in.
		 *    metaFields: { totalRecords: "see.example_total" } // (Optional) String locator of additional meta data.
		 * }
		 */
		setConfig: function(config) {
			this.config = config;
			if (this.loaded) {
				this._updateColumns();
				this._updateResponseSchema();
				this.loadData(this.state.rootItem._item_children_);
			}
		},

		/**
		 * Set table context.
		 * Table context is a data item, that corresponds to table root.
		 * Note: If table receives message, that its root item is removed from its parent,
		 *       it clears data and sets context to "none", "none".
		 * @param item - item that corresponds to table root.
		 * @param parent - root's parent
		 */
		setContext: function(item, parent) {
			this.state.rootItem = this.model.getItem(item);
			this.state.rootParent = this.model.getItem(parent);
			if (this.loaded)
				this.showMessageLoading();
		},

		/**
		 * Model update handler - item updated.
		 * Re-renders all views of changed item in table.
		 * @param args.item - updated item
		 */
		onItemUpdated: function(args) {
			var item = this.model.getItem(args.item);
			var found = false;
			var items = this.state.rootItem._item_children_;
			for (var i = 0; i < items.length; i++) {
				if(items[i]._item_name_ == item._item_name_) {
					items.splice(i, 1, item);
					found = true;
				}
			}
			if (found) {
				this.loadData(items);
			}
		},

		/** 
		 * Model update handler - children updated.
		 * It refreshes table data source and render new items.
		 * @param args.from - parent of updated items
		 * @param args.items - updated items
		 */
		onChildrenUpdated: function(args) {
			var parent = this.model.getItem(args.from);
			if(parent == this.state.rootItem) {
                parent._item_children_ = args.items;
                this.loadData(args.items);
            }
		},

		/**
		 * Model update handler - child added.
		 * It is only refresh all table.
		 * @param args.item - added item
		 * @param args.from - item, to which it was added
		 */
		onChildAdded: function(args) {
            var parent = this.model.getItem(args.from);
            if(parent == this.state.rootItem)
                this.loadData(parent._item_children_);
		},

		/**
		 * Model update handler - child removed.
		 * It is only refresh all table.
		 * @param args.item - deleted item
		 * @param args.from - item, from which it was deleted
		 */
		onChildDeleted: function(args) {
            var parent = this.model.getItem(args.from);
            if(parent == this.state.rootItem)
                this.loadData(parent._item_children_);
        },

        getSelectedRecords: function() {
        	var self = this;
        	return _.map(self.widgets.table.getSelectedRows(), function(item) {
        		return self.widgets.table.getRecord(item);
        	});
        },

        /**
         * when on ready
         * */
		onReady: function() {
			var self = this;

            this._setupDataSource();
            this._setupDataTable();

            this._setMessage("MSG_EMPTY", "message.dynamic-table.empty");
            this._setMessage("MSG_ERROR", "message.dynamic-table.error");
            this._setMessage("MSG_LOADING", "message.dynamic-table.loading");
            this._setMessage("MSG_SORTASC", "message.dynamic-table.sortasc");
            this._setMessage("MSG_SORTDESC", "message.dynamic-table.sortdesc");
            this._updatingErrorMsg = this.msg("message.dynamic-table.error");
            Dom.addClass(this.id, Citeck.widget.DynamicTable.CLASS_NAME);

			this.loaded = true;
			this._updateColumns();
			this._updateResponseSchema();
			if (this.deferredItems) this.loadData(this.deferredItems);


			var table = this.widgets.table;

			// SELECTION
			if (this.config.selection == "checkbox") {
				table.subscribe("checkboxClickEvent", function(event) {
					if (event.target.checked) {
						table.selectRow(event.target);
					} else { table.unselectRow(event.target) }
				});

				$("input[id*='select-all']:checkbox", table.getTheadEl()).on("change", function(event) {
					var checked = event.target.checked,
						selector = ":checkbox" + (checked ? ":not(:checked)" : ":checked");
					$(selector, table.getBody()).trigger("click");
				});
			};

			// PREVIEW
			if (this.config.preview) {
				var cell = cell = this.config.previewByClickOnCell || "cm_title";
				table.subscribe("cellClickEvent", function(args) {
					if (args.target.headers.indexOf(cell) != -1) {
						var record = table.getRecord(args.target),
							preview = Alfresco.util.ComponentManager.get(record.getId());

						if (!preview) {
							preview = new Citeck.UI.preview(record.getId(), { nodeRef: record.getData().nodeRef, renderImmediately: true });
							Alfresco.util.ComponentManager.register(preview);
						}

						preview.show();
					}
				});
			}

			// BEFORE RENDER
			if (this.config.beforeRender) {
				table.subscribe("beforeRenderEvent", this.config.beforeRender);
			}
		},

		/**
		 * It loads new data.
		 */
		loadData: function(items) {
			if (!this.loaded) {
				this.deferredItems = items;
			}
			else {
				var scope = this;
				if (scope.widgets.dataSource.responseType === YAHOO.util.DataSource.TYPE_JSARRAY)
					scope.widgets.dataSource.liveData = items;
				else
					scope.widgets.dataSource.liveData[this.config.responseSchema.resultsList] = items;
				scope.widgets.dataSource.sendRequest(
					"", {
						success: function(sRequest, oResponse, oPayload) {
							scope.widgets.table.onDataReturnInitializeTable.call(scope.widgets.table, sRequest, oResponse, oPayload);
							scope.widgets.table.set('sortedBy', scope.config.sortedBy);
						},
						failure: function(sRequest, oResponse) {
							if (scope._updatingErrorMsg)
								Alfresco.util.PopupManager.displayMessage({ text: scope._updatingErrorMsg });
						},
						scope: scope
					}
				);
			}
		},

		showMessageLoading: function() {
			this.widgets.table.showTableMessage(
					this.widgets.table.get("MSG_LOADING"),
					YAHOO.widget.DataTable.CLASS_LOADING);
		},

		showMessageEmpty: function() {
			this.widgets.table.showTableMessage(
					this.widgets.table.get("MSG_EMPTY"),
					YAHOO.widget.DataTable.CLASS_EMPTY);
		},

		/**
		 * Called on destroy.
		 * Unsubscribe from button events.
		 */
		destroy: function() {
			if (this.model)
				this.model.unsubscribeAll(this);
			if (this.widgets && this.widgets.table)
				this.widgets.table.unsubscribeAll(this);
			Citeck.widget.DynamicTable.superclass.destroy.call(this);
		},

		_updateColumns: function() {
			var columns = this.config.columns,
				table = this.widgets.table;
			if (columns && table) {
				var columnSet = this.widgets.table.getColumnSet(),
					columnDefinitions = columnSet.getDefinitions();
				// save sortedBy and reset it:
				var sortedBy = table.get("sortedBy");
				table.set("sortedBy", null);
				// first remove all columns:
				for(var i = columnDefinitions.length; i--; ) {
					var def = columnDefinitions[i],
						column = columnSet.getColumn(def.key);
					if(column != null) {
						table.removeColumn(column);
					}
				}
				// next add all columns:
				for(var i = 0, ii = columns.length; i < ii; i++) {
					var def = columns[i];
					if(def != null && def.key != null) {
						table.insertColumn(def, i);
					}
				}
				// reset sortedBy if there is such a key:
				if(sortedBy) {
					for(var i = 0, ii = columns.length; i < ii; i++) {
						var def = columns[i];
						if(def.key == sortedBy.key) {
							table.set("sortedBy", sortedBy);
							break;
						}
					}
				}
				// Refreshing MSG body, in other case, message shows in only one column, because of we construct current table in the constructor.
				var elTbody = this.widgets.table._elTbody;
				this.widgets.table._destroyMsgTbodyEl();
				this.widgets.table._elMsgTbody = null;
				this.widgets.table._elTbody = elTbody;
				this.widgets.table._initMsgTbodyEl(this.widgets.table._elTable);
			}
		},
		
		_updateResponseSchema: function() {
			if (this.config && this.widgets && this.widgets.dataSource) {
				if (typeof this.config.responseSchema !== 'undefined' && this.config.responseSchema !== null)
					this.widgets.dataSource.responseSchema = this.config.responseSchema;
				if (typeof this.config.responseType !== 'undefined' && this.config.responseType !== null)
					this.widgets.dataSource.responseType = this.config.responseType;
			}
		},

		_setupDataSource: function() {
			var ds = this.widgets.dataSource = new YAHOO.util.LocalDataSource(this.defaultData);
			ds.responseType = this.defaultResponseType;
			ds.responseSchema = this.defaultResponseSchema;
		},

		_setupDataTable: function() {
			var table = this.widgets.table = new YAHOO.widget.GroupedDataTable(
				this.id,
				this.defaultColumnConfig,
				this.widgets.dataSource,
				{
					groupBy: this.config.groupBy
				}
			);

 			table.doBeforeSortColumn = this.bind(function(column, dir) {
				this.fireEvent("sortColumnChange", {
					column: column.key,
					desc: dir == YAHOO.widget.DataTable.CLASS_DESC
				});
				return false;
			});
		},

		_setMessage: function(property, messageCode) {
			var msg = this.msg(messageCode);
			var table = this.widgets.table;
			if (msg && table)
				table.setAttributeConfig(property, { value: msg });
		}
		
	});

	/**
	 * DynamicTableControl class.
	 * It is controller of the MVC pattern.
	 */
	Citeck.widget.DynamicTableControl = function(htmlId, fieldId) {
		var name = "Citeck.widget.DynamicTableControl";
		Citeck.widget.DynamicTableControl.superclass.constructor.call(this, name, htmlId, null);
		this.loaded = false;
		this.deferredItem = null;
		this.fieldId = fieldId;
		this.htmlId = htmlId;

		YAHOO.Bubbling.on("formContainerDestroyed", this.onFormContainerDestroyed, this);
		YAHOO.Bubbling.on("metadataRefresh", this.onItemUpdated, this);
		YAHOO.Bubbling.on("folderDeleted", this.onItemUpdated, this);
		YAHOO.Bubbling.on("fileDeleted", this.onItemUpdated, this);
		YAHOO.Bubbling.on("actionNonContentButtonClicked", this.onActionButtonClicked, this);

	};

	YAHOO.extend(Citeck.widget.DynamicTableControl, Alfresco.component.Base, {
		// default values for options
		options: {
            // true if field is mandatory
            mandatory: false,

			// model configuration
			model: {},

			// columns configuration
			columns: {},

            // forms configuration
            forms: {
                nodeId: "nodeRef", // default: "nodeRef", but can equals any value
            },

            // temporary destination folder for created items
            destFolder: null,

            // type item, which is editing in current control (for "example: journal:criterion")
            itemId: null,

            // temporary destination folder for created items
            assocType: 'cm:contains',

            // field value delimiter (in multipleSelectMode)
            fieldValueDelim: ",",

			// data source configuration
			responseSchema: {},
			responseType: YAHOO.util.DataSource.TYPE_JSON,

			// root item
			rootNode: {},

            // selected items
            selectedItems: {},

            // custom select button label:
            btnAddTitle: null,

            // allow to select new objects:
            allowSelectAction: true,

            allowSearchAction: false,
            search: {
                itemType: null,
                itemKey: null,
                itemTitle: null,
                itemURL: null,
                itemURLresults: null,
                searchURL: null,
                searchURLresults: null,
                rootURL: null,
                rootURLresults: null,
                btnSearchTitle: null
            },

            // somthing object (field for debugging by console)
            dataForTest: {},

            //by default, does not display the cancel button
            showCancelButton: false
        },

        /**
         * Throwable helper
         * @msg message for exception in "throw"
         * */
        throwEx: function(msg) {
            throw "dynamic-table.js: " + msg;
        },

		/**
		 * Set options for this table
		 */
		setOptions: function(options) {
			Citeck.widget.DynamicTableControl.superclass.setOptions.call(this, options);
			return this;
		},

		/**
		 * Set messages for this table
		 */
		setMessages: function(messages) {
			Citeck.widget.DynamicTableControl.superclass.setMessages.call(this, messages);
			return this;
		},

        /**
         * ON READY
         * */
        onReady: function() {
            var _this_id = this.fieldId,
                _added_id = this.id + "-added",
                _rem_id = this.id + "-removed";

            // data fields:
            this.field = Dom.get(_this_id);
            this.fieldAdded = Dom.get(_added_id);
            this.fieldRemoved = Dom.get(_rem_id);

            this._initModel();
            // table initializing
            this._initTable();

            // fill model with original selected items:
            this.originalItems = this._deserialize(this.field.value);

            var itemsToAdd = this._deserialize(this.fieldAdded.value),
                itemsToRemove = this._deserialize(this.fieldRemoved.value),
                items = _.difference(_.uniq(_.union(this.originalItems, itemsToAdd)), itemsToRemove);

            // filling model from fields
            this.selectItems(items);

            this._initButtons();

			// tell the world, that we are ready:
			YAHOO.Bubbling.fire("dynamicTableReady", {
				eventGroup: this
			});
			this.loaded = true;

			if (this.deferredItem)
				this.onItemUpdated(this.deferredItem);
		},

        /**
         * Initialization tool-bar for table..
         * */
        _initButtons: function() {
            var scope = this;
            // initialize widgets
            if(this.options.allowSelectAction) {
                // -- button ADD ITEM (select item)
                this.widgets.buttonSelect = new YAHOO.widget.Button({
                    type: "button",
                    container: this.id + "-itemGroupActions",
                    label: '<span class="btn-add">&nbsp;</span>',
                    onclick: {
                        scope: this,
                        fn: scope.onButtonAddClick,
                    },
                });
                if (this.widgets.buttonSelect) {
                    var btn = this.widgets.buttonSelect;
                    btn.set("title", this.options.btnAddTitle || Alfresco.messages.scope["title.table-children.addItem"]);
                }
            }
            if (this.options.allowSearchAction) {
                this.widgets.buttonSearch = new YAHOO.widget.Button({
                    type: "button",
                    container: this.id + "-itemGroupActions",
                    label: '<span class="btn-search">&nbsp;</span>',
                    onclick: {
                        scope: this,
                        fn: scope.onButtonSearchClick,
                    },
                });
                if (this.widgets.buttonSearch) {
                    var btn = this.widgets.buttonSearch;
                    btn.set("title", this.options.search.btnSearchTitle || Alfresco.messages.scope["title.table-children.searchItem"]);
                }
            }
        },

        /**
         * Item created handler
         * This handler updates the model and fields states
         * */
        onItemCreated: function(args) {
            if (!args[0].json.persistedObject)
                return;
            // getting new item nodeRef
            var createdItem = args[0].json.persistedObject;
            // adding new item in model
            var newItem = {};
            newItem["_item_name_"] = createdItem;
            newItem[this.options.forms.nodeId] = createdItem;
            this.model.addItem(newItem, "selected-items", false);
        },

        isNodeRef: function(str) {
            return str && (str.indexOf('workspace://') === 0 || str.indexOf('alfresco://') === 0 || str.indexOf('archive://') === 0);
        },

        /**
         * Event handler - button "Add" (or create) was clicked
         * Shows the creation dialog for current item-type
         */
        onButtonAddClick: function() {
            if (this.isNodeRef(this.options.destFolder)) {
                this.openCreateFormDialog(this.options.destFolder);
            }
            else {
                this.getDestinationAndProcess(
                        this.options.destFolder,
                        this.openCreateFormDialog);
            }
        },

        onButtonSearchClick: function() {
            var scope = this;
            this.options.search.onPanelButtonAdd = function(opts, items) {
                for(var i = 0; i < items.length; i++) {
                    var item = items[i];
                    var newItem = {};
                    newItem["_item_name_"] = item.nodeRef;
                    newItem[scope.options.forms.nodeId] = item.nodeRef;
                    scope.model.addItem(newItem, "selected-items", false);
                }
            };
            if (this.isNodeRef(this.options.destFolder)) {
                Citeck.widget.ButtonPanel.Commands.onPanelButtonAdd(this.options.search);
            }
            else {
                var props = this.options.search;
                if (props) {
                    this.getDestinationAndProcess(
                            scope.options.destFolder,
                            function(nodeRef) {
                                for (var i in props) {
                                    if (!props.hasOwnProperty(i))
                                        continue;
                                    var v = '' + props[i];
                                    scope.options.search[i] = v.replace(/[[destFolder]]/g, nodeRef);
                                }
                                Citeck.widget.ButtonPanel.Commands.onPanelButtonAdd(scope.options.search);
                            });
                }
            }
        },

        getDestinationAndProcess: function(url, func) {
            var scope = this;
            Alfresco.util.Ajax.jsonGet({
                url: url,
                successCallback: {
                    fn: function(response) {
                        if (response && response.json &&
                                response.json.data && this.isNodeRef(response.json.data)) {
                            func.call(scope, response.json.data);
                        }
                        else {
                            Alfresco.util.PopupManager.displayMessage({
                                text: scope.msg("error.can.not.get.destination")
                            });
                        }
                    },
                    scope: this
                },
                failureMessage: this.msg("error.can.not.get.destination")
            });
        },

        openCreateFormDialog: function(destination) {
            var scope = this;
            var itemId = scope.options.itemId,
                formId = null,
                name = null,
                marker = null,
                htmlid = Alfresco.util.generateDomId();
            var dlg = new Citeck.widget.CreateFormDialog(
                    htmlid, itemId, formId, destination, scope.options.assocType, marker, name);
            dlg.setOptions({width: '62em'});
            dlg.show();
            dlg.subscribe("itemCreated", function() { scope.onItemCreated(arguments); }, this, true);
        },

        /**
         * Must be called when some table-line is edited successfully. This function
         * invokes refreshing for edited item.
         * @nodeRef is nodeRef of edited item
         * */
        onItemEdited: function(nodeRef) {
            var editedItem = {};
            editedItem["_item_name_"] = nodeRef;
            editedItem[this.options.forms.nodeId] = nodeRef;
            var item = this.model.getItem(editedItem);
            this.model.updateItem(item, false, null);
        },

        /**
         * Action Button handler
         * @evnId is the event name, that was sent through fire-event
         * @e[1].nodeRef nodeRef
         * @e[1].eventType button type
         * @e[1].source is the reference to button which generates this event
         * @cntrl is scope
         * */
        onActionButtonClicked: function (evnId, e, cntrl) {
            var scope = this;
            var el = e[1];
            if (!el) throw "table-children: answer inside action-event is not defined!";
            var itemForSearch = {"_item_name_": el.nodeRef, "nodeRef": el.nodeRef };
            var delButton = Dom.getElementBy(function(element){ return element.id === el.elementId; }, el.elementTag, this.id);
            if (!this.model || !delButton || !this.model.getItem(itemForSearch)) {
                //console.dir("this.model not contains this nodeRef!");
                return;
            }
            if (el.eventType === "edit") {
                // if event is "edit' then we must show modal-editor:
                Alfresco.util.PopupManager.displayForm({
                    title: this.msg('button.edit'),
                    properties: {
                        mode: "edit",
                        itemKind: "node",
                        itemId: el.nodeRef,
                        formId: "",
                        submitType: "json",
                        showSubmitButton: true,
                        showCancelButton: scope.options.showCancelButton,
                        destination: scope.options.destFolder
                    },
                    success: {
                        scope: this,
                        fn: function () {
                            if (!arguments[0] || !arguments[0].json || !arguments[0].json.persistedObject)
                                throw "Edited item nodeRef not exists!";
                            var nodeRef = arguments[0].json.persistedObject;
                            scope.onItemEdited(nodeRef)
                        }
                    }
                });
            } else if (el.eventType === "remove") {
                this.removeItem({ "item": el });
            } else if (el.eventType === "start-workflow") {
				
                document.location.href = '/share/page/start-specified-workflow?'+el.wf_params;
            }
        },

        /**
         * Deserialize comma-separated string.
         **/
        _deserialize: function(value) {
            return value ? value.split(this.options.fieldValueDelim) : [];
        },

        /**
         * Set items to be selected.
         * Items are specified as identifiers, other information is actively retrieved from repository.
         * @param items - array of item identifiers or string of identifiers (serialized, as it is stored in field value)
         */
        selectItems: function(items) {
            if(typeof items != "object") {
                items = this._deserialize(items);
            }

            // update items:
            var selected = this.model.getItem("selected-items");
            var children = [];
            for(var i = 0; i < items.length; i++) {
                children[i] = {};
                children[i]["_item_name_"] = items[i];
                children[i][this.options.forms.nodeId] = items[i];
                children[i]["selected-index"] = i;
            }
            selected._item_children_ = children;

            this.model.updateChildren("selected-items", false);
            if(this.initialized) {
                this._updateFields();
            }
        },

		/**
		 * Event handler - it updates children when rootItem is updated
		 */
		onItemUpdated: function(args) {
			var item = args && args.item ? args.item : "selected-items";
			if (this.loaded) {
				this.widgets.tableView.showMessageLoading();
				this.model.updateChildren(item, false);
			}
			else {
				this.deferredItem = item;
			}
		},

		/**
		 * Event handler - current form is being destroyed.
		 * Destroy the component too.
		 */
		onFormContainerDestroyed: function(layer, args) {
			this.destroy();
		},

        /**
         * Event handler - child was deleted.
         * If item was added to "selected-items", unset "selected" field.
         */
        removeItem: function(args) {
            var item = this.model.getItem(args.item);
            this.model.deleteItem(item, "selected-items", true);
        },

        /**
         * Event handler - child was added.
         * If item was added to "selected-items", set "selected" field.
         */
        onChildAdded: function(args) {
            if(this.model.getItemName(args.from) == "selected-items") {
                var item = this.model.getItem(args.item);
                item.selected = "yes";
                this._updateFields();
            }
        },

        /**
         * Event handler - child was deleted.
         * If item was deleted from "selected-items", unset "selected" field.
         */
        onChildDeleted: function(args) {
            if(this.model.getItemName(args.from) == "selected-items") {
                var item = this.model.getItem(args.item);
                item.selected = "no";
                this._updateFields();
            }
        },

        /**
         * Event handler - on children was updated (fire event "childrenUpdated")
         * */
        onChildrenUpdated: function(args) {
            if(this.model.getItemName(args.from) == "selected-items") {
                this._updateFields();
            }
        },

		/**
		 * Called on destroy.
		 * Unsubscribe from button events.
		 */
		destroy: function() {
			YAHOO.Bubbling.unsubscribe("formContainerDestroyed", this.onFormContainerDestroyed, this);
			YAHOO.Bubbling.unsubscribe("metadataRefresh", this.onItemUpdated, this);
			YAHOO.Bubbling.unsubscribe("folderDeleted", this.onItemUpdated, this);
			YAHOO.Bubbling.unsubscribe("fileDeleted", this.onItemUpdated, this);
			if (this.model) {
				this.model.unsubscribeAll();
				delete this.model;
			}
			if (this.widgets) {
				if (this.widgets.tableView)
					this.widgets.tableView.unsubscribeAll(this);
			}
			Citeck.widget.DynamicTableControl.superclass.destroy.call(this);
		},

		_initModel: function() {
			// take model from dialog
			var modelConfig = this.options.model;
			this.model = new Citeck.util.HierarchyModel(this.name);
			this.model.setConfig(modelConfig);

			this.model.createSpecialItem(
					this.options.rootNode,
					{ name: "root", keys: ["root"] } );

			this.model.createSpecialItem(this.options.selectedItems, {
				name: "selected-items",
				keys: ["selected-items"]
			});
			// it is called when table item is updated
			this.model.subscribe("itemUpdated", this.onItemUpdated, this, true);
			// it is called when root element is updated
			this.model.subscribe("childrenUpdated", this.onChildrenUpdated, this, true);
			// it is called when table item is added
			this.model.subscribe("childAdded", this.onChildAdded, this, true);
			// it is called when table item is deleted
			this.model.subscribe("childDeleted", this.onChildDeleted, this, true);
		},

		_initTable: function() {
			var self = this,
				tableView = this.widgets.tableView = new Citeck.widget.DynamicTable(this.id + "-view", this.model, this.name),
				config = { responseSchema: this.options.responseSchema, columns: this.options.columns };

			if (this.options.selection == "checkbox") {
				config.columns.unshift({
					key: "checkbox-selection",
					label: '<input type="checkbox" id="' + this.id + '-select-all"/>',
		            formatter: function(elCell, oRecord) {
		            	var checked = this.getSelectedRows().indexOf(oRecord.getId()) != -1,
		            		disabled = oRecord.getData().disabled;

		            	elCell.innerHTML = '<input type="checkbox" ' +  (checked && !disabled ? 'checked' : '') + '' + (disabled ? 'disabled' : '') +  ' />';
		            },
					resizeable: false 
				});
				
				config.selection = this.options.selection
			}

			if (this.options.preview) {
				config.preview = this.options.preview;

				if (this.options.previewByClickOnCell)
					config.previewByClickOnCell = this.options.previewByClickOnCell;
			}

			if (this.options.beforeRender) {
				config.beforeRender = this.options.beforeRender;
			}

			tableView.setConfig(config);
			tableView.setContext("selected-items", "none");
		},

        _updateFields: function() {
            var selectedItems = this.model.getItem("selected-items")._item_children_;
            // calculate current, added, removed
            var current = [],
                added = [],
                removed = [].concat(this.originalItems);
            for(var i in selectedItems) {
                if(!selectedItems.hasOwnProperty(i)) continue;
                var id = selectedItems[i]["_item_name_"];
                current.push(id);
                var index = removed.indexOf(id);
                if(index == -1) {
                    added.push(id);
                } else {
                    removed.splice(index, 1);
                }
            }
            var delim = this.options.fieldValueDelim;
            this.field.value = current.join(delim);
            if (this.fieldAdded)
                this.fieldAdded.value = added.join(delim);
            if (this.fieldRemoved)
                this.fieldRemoved.value = removed.join(delim);
            // notify mandatory constraint checker, if necessary
            if (this.options.mandatory) {
                YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
            }
        },

	});
	YAHOO.lang.augmentObject(Citeck.widget.DynamicTableControl.prototype, Citeck.util.ErrorManager.prototype);

	Citeck.widget.FileSizeFormatter = Citeck.widget.FileSizeFormatter || {};
	Citeck.widget.FileSizeFormatter.get = function() {
		return function(elCell, oRecord, oColumn, oData) {
			var value = parseInt(oData);
			if (!isNaN(value))
				elCell.innerHTML = Alfresco.util.formatFileSize(value);
		}
	};

})();
