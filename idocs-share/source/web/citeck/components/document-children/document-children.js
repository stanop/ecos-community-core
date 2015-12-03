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
	Citeck = typeof Citeck != "undefined" ? Citeck : {};
	Citeck.widget = Citeck.widget || {};

	Citeck.widget.DocumentChildren = function(htmlid) {
		Citeck.widget.DocumentChildren.superclass.constructor.call(this, "Citeck.widget.DocumentChildren", htmlid, null);
		this.loaded = false;
		this.deferredItem = null;
		this.model = null;
		this.widgets = {};
		this.widgets.tableView = null;

		YAHOO.Bubbling.on("metadataRefresh", this.onItemUpdated, this);
		YAHOO.Bubbling.on("folderDeleted", this.onItemUpdated, this);
		YAHOO.Bubbling.on("fileDeleted", this.onItemUpdated, this);
	};
	YAHOO.extend(Citeck.widget.DocumentChildren, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.DocumentChildren.prototype, {
		// default values for options
		options: {
			// parent node reference
			nodeRef: null,

			// it is web-script URL, which returns data of represented documents
			childrenUrl: null,

			// it is a column description in YAHOO.widget.DataTable column definition format
			columns: null,

			// it is a data structure description in YAHOO.util.LocalDataSource format
			responseSchema: null,
			responseType: null,
			
			groupBy: null,
			groupTitle: null,

            hideEmpty: false,

			childrenFormat: "item"
		},

		/**
		 * Set options for this document children
		 */
		setOptions: function(options) {
			Citeck.widget.DocumentChildren.superclass.setOptions.call(this, options);
			return this;
		},

		/**
		 * Set messages for this document children
		 */
		setMessages: function(messages) {
			Citeck.widget.DocumentChildren.superclass.setMessages.call(this, messages);
			return this;
		},

		onItemUpdated: function(args) {
			var item = args && args.item ? args.item : "root";
			if (this.loaded) {
				this.widgets.tableView.showMessageLoading();
				this.model.updateChildren(item, false);
			}
			else {
				this.deferredItem = item;
			}
		},

        onChildrenUpdated: function (args) {
            if (this.options.hideEmpty && args.items && args.items.length == 0) {
                $("#" + this.id).show();
            } else {
                $("#" + this.id).hide();
            }
        },

		onReady: function() {
			var root = this.options.nodeRef ? this.options.nodeRef : "root",
				rootNode = {
					nodeRef: root,
					nodeRefForURL: root.replace("://", "/")
				};
			this._initModel(rootNode);
			this._initTable(rootNode);
			this.loaded = true;
			if (this.deferredItem)
				this.onItemUpdated(this.deferredItem);
		},

		/**
		 * Called on destroy.
		 * Unsubscribe from button events.
		 */
		destroy: function() {
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
			Citeck.widget.DocumentChildren.superclass.destroy.call(this);
		},

		_initModel: function(rootNode) {
			var modelConfig = {
				formats: {
					"item": {
						name: "{nodeRef}",
						keys: [ "item" ],
						calc: function(item) {
							item.nodeRefForURL = item.nodeRef && item.nodeRef.replace("://", "/");
						},
					},
					"task": {
						name: "{taskId}",
						keys: ["task"],
						calc: function(item) {
							item.nodeRefForURL = item.nodeRef && item.nodeRef.replace("://", "/");
						}
					}
				},
				item: {
				},
				children: {
					"root": {
						"format": this.options.childrenFormat ? this.options.childrenFormat : "item",
						"get": this.options.childrenUrl,
						"resultsList": this.options.responseSchema.resultsList,
					},
				},
				titles: {
					"root": "{title}",
					"item": "{displayName}"
				}
			};
			this.model = new Citeck.util.HierarchyModel(this.name);
			this.model.setConfig(modelConfig);
			this.model.createSpecialItem(
					rootNode,
					{ name: "root", keys: ["root"] } );
			this.model.subscribe("itemUpdated", this.onItemUpdated, this, true);
            this.model.subscribe("childrenUpdated", this.onChildrenUpdated, this, true);
		},

		_initTable: function(rootNode) {
			var table = this.widgets.tableView = new Citeck.widget.DynamicTable(
					this.id + "-view",
					this.model,
					this.name);
			var groupBy = this.options.groupBy ? 
			        [{
			            id: this.options.groupBy,
			            label: this.options.groupTitle
			        }] : null;
			table.setConfig({
				columns: this.options.columns,
				responseSchema: this.options.responseSchema,
				responseType: this.options.responseType,
				groupBy: groupBy
			});
			table.setContext(rootNode, "none");
			this.model.updateChildren(rootNode, false);
		}

	});

})();
